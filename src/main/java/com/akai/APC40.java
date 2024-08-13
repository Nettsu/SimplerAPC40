package com.akai;

import com.bitwig.extension.api.util.midi.ShortMidiMessage;
import com.bitwig.extension.callback.ShortMidiMessageReceivedCallback;
import com.bitwig.extension.controller.api.*;
import com.bitwig.extension.callback.*;
import java.util.function.*;

import static com.akai.SimplerAPC40Extension.*;

public class APC40 {

  static final int SIDE_KNOBS_CC = 0x10;
  static final int TOP_KNOBS_CC = 0x30;

  static final int ARM_NOTE = 0x30;
  static final int SOLO_NOTE = 0x31;
  static final int TRACK_NUM_NOTE = 0x32;
  static final int SELECT_NOTE = 0x33;
  static final int CLIP_STOP_NOTE = 0x34;
  static final int DEVICE_LEFT_NOTE = 0x3A;
  static final int DEVICE_RIGHT_NOTE = 0x3B;
  static final int BANK_LEFT_NOTE = 0x3C;
  static final int BANK_RIGHT_NOTE = 0x3D;
  static final int DEVICE_ON_OFF_NOTE = 0x3E;
  static final int DEVICE_LOCK_NOTE = 0x3F;
  static final int CLIP_DEV_VIEW_NOTE = 0x40;
  static final int DETAIL_VIEW_NOTE = 0x41;
  static final int AB_NOTE = 0x42;

  static final int MASTER_NOTE = 0x50;
  static final int STOP_ALL_NOTE = 0x51;
  static final int SCENE_NOTE = 0x52;
  static final int PAN_NOTE = 0x57;
  static final int SEND_A_NOTE = 0x58;
  static final int SEND_B_NOTE = 0x59;
  static final int METRONOME_NOTE = 0x5A;
  static final int PLAY_NOTE = 0x5B;
  static final int STOP_NOTE = 0x5C;
  static final int RECORD_NOTE = 0x5D;
  static final int UP_NOTE = 0x5E;
  static final int DOWN_NOTE = 0x5F;
  static final int RIGHT_NOTE = 0x60;
  static final int LEFT_NOTE = 0x61;
  static final int SHIFT_NOTE = 0x62;
  static final int TAP_TEMPO_NOTE = 0x63;
  static final int NUGDE_LESS_NOTE = 0x64;
  static final int NUDGE_MORE_NOTE = 0x65;
  static final int SESSION_REC_NOTE = 0x66;
  static final int BANK_NOTE = 0x67;

  static final int VOLUME_CC = 0x07;
  static final int TEMPO_CC = 0x0D;
  static final int MASTER_CC = 0x0E;
  static final int CROSSFADER_CC = 0x0F;

  public static final int RING_INIT = -1;
  public static final int RING_OFF = 0;
  public static final int RING_SINGLE = 1;
  public static final int RING_VOLUME = 2;
  public static final int RING_PAN = 3;

  static final int TOP_KNOBS_DEVICE = 0;
  static final int TOP_KNOBS_SEND1 = 1;
  static final int TOP_KNOBS_SEND2 = 2;
  static final int SIDE_KNOBS_GLOBAL = 3;
  static final int SIDE_KNOBS_DEVICE = 4;

  static final int CLIP_STATE_STOPPED = 0;
  static final int CLIP_STATE_PLAYING = 1;
  static final int CLIP_STATE_RECORDS = 2;

  static final RGBState STOP_QUEUED_COLOUR = RGBState.WHITE_PULSE;
  static final RGBState INACTIVE_COLOUR = RGBState.DARKGREY;
  static final RGBState PLAY_COLOUR = RGBState.WHITE;
  static final RGBState ON_COLOUR = RGBState.WHITE;
  static final RGBState PLAY_QUEUED_COLOUR = RGBState.WHITE_BLINK;
  static final RGBState REC_COLOUR = RGBState.RED;
  static final RGBState REC_QUEUED_COLOUR = RGBState.RED_BLINK;
  static final RGBState STOP_ACTIVE_COLOUR = RGBState.DARKGREY;
  static final RGBState STOP_INACTIVE_COLOUR = RGBState.OFF;
  static final RGBState STOP_INACTIVE_QUEUED_COLOUR = RGBState.DARKGREY_BLINK;

  private Track[] mChannels = {
    mTrackBank.getItemAt(0),
    mTrackBank.getItemAt(1),
    mTrackBank.getItemAt(2),
    mTrackBank.getItemAt(3),
    mTrackBank.getItemAt(4),
    mTrackBank.getItemAt(5),
    mTrackBank.getItemAt(6),
    mTrackBank.getItemAt(7),
  };

  private int[][] mKnobTypes = { 
    {
      RING_PAN, RING_PAN, RING_PAN, RING_PAN, RING_PAN, RING_PAN, RING_PAN, RING_PAN
    },
    {
      RING_VOLUME, RING_VOLUME, RING_VOLUME, RING_VOLUME, RING_VOLUME, RING_VOLUME, RING_VOLUME, RING_VOLUME
    },
    {
      RING_VOLUME, RING_VOLUME, RING_VOLUME, RING_VOLUME, RING_VOLUME, RING_VOLUME, RING_VOLUME, RING_VOLUME
    },
    {
      RING_VOLUME, RING_VOLUME, RING_VOLUME, RING_VOLUME, RING_VOLUME, RING_PAN, RING_PAN, RING_VOLUME
    },
    {
      RING_SINGLE, RING_SINGLE, RING_SINGLE, RING_SINGLE, RING_SINGLE, RING_SINGLE, RING_SINGLE, RING_SINGLE
    }
  };

  private int[][] mKnobDAWValues = new int[5][8];
  private int[][] mKnobAPCValues = new int[5][8];

  private Parameter[][] mKnobControls = {
    // first row (Pan / Device)
    {
      mRemoteControls[0].getParameter(0),
      mRemoteControls[1].getParameter(0),
      mRemoteControls[2].getParameter(0),
      mRemoteControls[3].getParameter(0),
      mRemoteControls[4].getParameter(0),
      mRemoteControls[5].getParameter(0),
      mRemoteControls[6].getParameter(0),
      mRemoteControls[7].getParameter(0)
    },
    // second row (Send A)
    {
      mTrackBank.getItemAt(0).sendBank().getItemAt(0),
      mTrackBank.getItemAt(1).sendBank().getItemAt(0),
      mTrackBank.getItemAt(2).sendBank().getItemAt(0),
      mTrackBank.getItemAt(3).sendBank().getItemAt(0),
      mTrackBank.getItemAt(4).sendBank().getItemAt(0),
      mTrackBank.getItemAt(5).sendBank().getItemAt(0),
      mTrackBank.getItemAt(6).sendBank().getItemAt(0),
      mTrackBank.getItemAt(7).sendBank().getItemAt(0)
    },
    // third row (Send B)
    {
      mTrackBank.getItemAt(0).sendBank().getItemAt(1),
      mTrackBank.getItemAt(1).sendBank().getItemAt(1),
      mTrackBank.getItemAt(2).sendBank().getItemAt(1),
      mTrackBank.getItemAt(3).sendBank().getItemAt(1),
      mTrackBank.getItemAt(4).sendBank().getItemAt(1),
      mTrackBank.getItemAt(5).sendBank().getItemAt(1),
      mTrackBank.getItemAt(6).sendBank().getItemAt(1),
      mTrackBank.getItemAt(7).sendBank().getItemAt(1)
    },
    // side knobs bank A (global remotes)
    {
      mMasterRemotes.getParameter(0),
      mMasterRemotes.getParameter(1),
      mMasterRemotes.getParameter(2),
      mMasterRemotes.getParameter(3),
      mMasterRemotes.getParameter(4),
      mMasterRemotes.getParameter(5),
      mMasterRemotes.getParameter(6),
      mMasterRemotes.getParameter(7),
    },
    // side knobs bank B (device remotes)
    {
      mEditorRemoteControls.getParameter(0),
      mEditorRemoteControls.getParameter(1),
      mEditorRemoteControls.getParameter(2),
      mEditorRemoteControls.getParameter(3),
      mEditorRemoteControls.getParameter(4),
      mEditorRemoteControls.getParameter(5),
      mEditorRemoteControls.getParameter(6),
      mEditorRemoteControls.getParameter(7)
    },
  };

  // track number button and A|B buttons control the following
  private Parameter[] mPadControls = {
    mUserControlBank.getControl(0),
    mUserControlBank.getControl(1),
    mUserControlBank.getControl(2),
    mUserControlBank.getControl(3),
    mUserControlBank.getControl(4),
    mUserControlBank.getControl(5),
    mUserControlBank.getControl(6),
    mUserControlBank.getControl(7)
  };

  private int[] mPadControlsValue = {
    0, 0, 0, 0, 0, 0, 0, 0
  };

  private int mTopKnobControl;
  private int mSideKnobControl;

  private MidiIn mMidiIn;
  private MidiOut mMidiOut;

  public void init() {
    mMidiIn = mAPCMidiIn;
    mMidiOut = mAPCMidiOut;

    mMidiIn.setMidiCallback((ShortMidiMessageReceivedCallback) msg -> onMidi(msg));
    mMidiOut.sendSysex("F0 7E 7F 06 01 F7"); // send introduction message
    mMidiOut.sendSysex("F0 47 7F 29 60 00 04 41 00 00 00 F7"); // set mode

    mTopKnobControl = TOP_KNOBS_SEND1;
    mSideKnobControl = SIDE_KNOBS_GLOBAL;

    for (int i = 0; i < 8; i++) {
      final int track_idx = i;
      Track track = mChannels[i];

      track.solo().addValueObserver(solo -> {
          mMidiOut.sendMidi((MSG_NOTE_ON << 4) | track_idx, SOLO_NOTE, solo ? 1 : 0);
      });

      track.mute().addValueObserver(mute -> {
        if (mShift)
          mMidiOut.sendMidi((MSG_NOTE_ON << 4) | track_idx, ARM_NOTE, mute ? 1 : 0);
      });

      track.arm().addValueObserver(arm -> {
        if (!mShift)
          mMidiOut.sendMidi((MSG_NOTE_ON << 4) | track_idx, ARM_NOTE, arm ? 1 : 0);
      });

      track.addIsSelectedInMixerObserver(selected -> {
        mMidiOut.sendMidi((MSG_NOTE_ON << 4) | track_idx, SELECT_NOTE, selected ? 1 : 0);
      });

      for (int bank = 0; bank < mKnobControls.length; bank++) {
        final int bank_idx = bank;
        mKnobControls[bank_idx][track_idx].value().addValueObserver(128, value -> {
          mKnobDAWValues[bank_idx][track_idx] = value;
          if (mKnobAPCValues[bank_idx][track_idx] != value)
            setKnob(bank_idx, track_idx, value);
        });
      }

      mPadControls[track_idx].value().addValueObserver(4, value -> {
        final int value_int = (int)Math.round(value);
        mPadControlsValue[track_idx] = value_int;
        mMidiOut.sendMidi((MSG_NOTE_ON << 4) | track_idx, TRACK_NUM_NOTE, (value_int & 1) == 1 ? 127 : 0);
        mMidiOut.sendMidi((MSG_NOTE_ON << 4) | track_idx, AB_NOTE, (value_int & 2) == 2 ? 127 : 0);
      });

      final ClipLauncherSlotBank slotBank = track.clipLauncherSlotBank();

      track.exists().addValueObserver(exists -> { updateAllLED(); });

      track.isStopped().addValueObserver(stopped -> {
        mHost.println("isStopped: " + stopped);
        if (stopped)
          mMidiOut.sendMidi((MSG_NOTE_ON << 4) | track_idx, CLIP_STOP_NOTE, 0);
        else
          mMidiOut.sendMidi((MSG_NOTE_ON << 4) | track_idx, CLIP_STOP_NOTE, 1);
      });

      track.isQueuedForStop().addValueObserver(stopQueued -> {
        mHost.println("isQueuedForStop: " + stopQueued);
        if (stopQueued && !track.isStopped().get())
          mMidiOut.sendMidi((MSG_NOTE_ON << 4) | track_idx, CLIP_STOP_NOTE, 2);
      });
      
      slotBank.addColorObserver((idx, r, g, b) -> { updateColumnLED(track_idx); });

      slotBank.addHasContentObserver((idx, hasContent) -> {
        final RGBState slot_colour = new RGBState(slotBank.getItemAt(idx).color().get());
        RGBState.send(mMidiOut, posToNote(idx, track_idx), hasContent ? slot_colour : RGBState.OFF);
      });

      slotBank.addPlaybackStateObserver((idx, state, queued) -> {
        ClipLauncherSlot slot = slotBank.getItemAt(idx);
        updateClipLED(idx, track_idx, slot, state, queued);
      });
    }

    setupButtonObserver(mTransport.isPlaying(), PLAY_NOTE);
    setupButtonObserver(mTransport.isMetronomeEnabled(), METRONOME_NOTE);
    setupButtonObserver(mTransport.isArrangerRecordEnabled(), RECORD_NOTE);
    setupButtonObserver(mTransport.isArrangerAutomationWriteEnabled(), SESSION_REC_NOTE);
    setupButtonObserver(mCursorDevice.isEnabled(), DEVICE_ON_OFF_NOTE);
    setupButtonObserver(mCursorDevice.isWindowOpen(), DETAIL_VIEW_NOTE);
    
    mMasterTrack.addIsSelectedInMixerObserver(selected -> {
      mMidiOut.sendMidi((MSG_NOTE_ON << 4), MASTER_NOTE, selected ? 1 : 0);
    });

    mCursorDevice.position().addValueObserver(val -> {
      updateKnobIndicators();
      updateAllKnobLEDs();
    });

    updateAllKnobLEDs();
    updateTopKnobSelector();
    updateSideButtons();
  }

  public void setupButtonObserver(BooleanValue value, int note) {
    value.addValueObserver(val -> {
      mMidiOut.sendMidi((MSG_NOTE_ON << 4), note, val ? 1 : 0); 
    });
  }

  public void exit() {
  }

  private void updateColumnLED(int trackIdx) {
    if (trackIdx < NUM_TRACKS) {
      Track track = mTrackBank.getItemAt(trackIdx);
      ClipLauncherSlotBank slotBank = track.clipLauncherSlotBank();
      for (int i = 0; i < NUM_SCENES; i++) {
        ClipLauncherSlot slot = slotBank.getItemAt(i); 
        if (slot.isPlaybackQueued().get())
          updateClipLED(i, trackIdx, slot, CLIP_STATE_PLAYING, true);
        else if (slot.isPlaying().get())
          updateClipLED(i, trackIdx, slot, CLIP_STATE_PLAYING, false);
        else if (slot.isStopQueued().get())
          updateClipLED(i, trackIdx, slot, CLIP_STATE_STOPPED, true);
        else if (slot.isRecordingQueued().get())
          updateClipLED(i, trackIdx, slot, CLIP_STATE_RECORDS, true);
        else if (slot.isRecording().get())
          updateClipLED(i, trackIdx, slot, CLIP_STATE_RECORDS, false);
        else
          updateClipLED(i, trackIdx, slot, CLIP_STATE_STOPPED, false);
      }
    }
  }

  private int posToNote(int row, int col) {
    return col + (NUM_SCENES - row - 1) * NUM_TRACKS;
  }

  private int noteToCol(int note) {
    return note % NUM_TRACKS;
  }

  private int noteToRow(int note) {
    return NUM_SCENES - 1 - (note / NUM_TRACKS);
  }

  private void updateClipLED(int row, int col, ClipLauncherSlot slot, int state, boolean queued) {
    RGBState slot_colour = new RGBState(slot.color().get());
    RGBState play_colour = RGBState.WHITE; //new RGBState(slot.color().get(), RGBState.RGB_TYPE_PULSING);
    RGBState play_queued_colour = new RGBState(slot.color().get(), RGBState.RGB_TYPE_PULSING_FAST);
    RGBState stop_queued_colour = play_colour;
    switch (state) {
      case CLIP_STATE_STOPPED:
        RGBState.send(mMidiOut, posToNote(row, col), queued ? stop_queued_colour : slot_colour);
        break;
      case CLIP_STATE_PLAYING:
        RGBState.send(mMidiOut, posToNote(row, col), queued ? play_queued_colour : play_colour);
        break;
      case CLIP_STATE_RECORDS:
        break;
    }
  }

  private void updateSideButtons() {
    mMidiOut.sendMidi((MSG_NOTE_ON << 4) | 0, BANK_NOTE, mSideKnobControl == SIDE_KNOBS_GLOBAL ? 1 : 0);
    updateTopKnobSelector();
  }

  private void setKnob(int bank, int index, int value) {
    updateKnobLEDs(bank, index, value);
  }

  private void updateKnobIndicators() {
    for (int i = 0; i < NUM_TRACKS; i++) {
      for (int bank = 0; bank < mKnobControls.length; bank++)
        mKnobControls[bank][i].setIndication(false);
    }

    for (int i = 0; i < NUM_TRACKS; i++) {
      mKnobControls[mTopKnobControl][i].setIndication(true);
      mKnobControls[mSideKnobControl][i].setIndication(true);
    }
  }

  private void updateAllLED() {
    updateTrackButtons();
    for (int i = 0; i < NUM_TRACKS; i++) updateColumnLED(i);
  }

  private void updateTrackButtons() {
    for (int i = 0; i < 8; i++) {
      mMidiOut.sendMidi((MSG_NOTE_ON << 4) | i, SOLO_NOTE, mChannels[i].solo().getAsBoolean() ? 1 : 0);
      if (!mShift)
        mMidiOut.sendMidi((MSG_NOTE_ON << 4) | i, ARM_NOTE, mChannels[i].arm().getAsBoolean() ? 1 : 0);
      else
        mMidiOut.sendMidi((MSG_NOTE_ON << 4) | i, ARM_NOTE, mChannels[i].mute().getAsBoolean() ? 1 : 0);
    }
  }

  private void updateTopKnobSelector() {
    mMidiOut.sendMidi((MSG_NOTE_ON << 4), PAN_NOTE, mTopKnobControl == TOP_KNOBS_DEVICE ? 1 : 0);
    mMidiOut.sendMidi((MSG_NOTE_ON << 4), SEND_A_NOTE, mTopKnobControl == TOP_KNOBS_SEND1 ? 1 : 0);
    mMidiOut.sendMidi((MSG_NOTE_ON << 4), SEND_B_NOTE, mTopKnobControl == TOP_KNOBS_SEND2 ? 1 : 0);
    updateKnobIndicators();
  }

  private void processNote(int note, int velocity, int channel, boolean noteOn) {
    // switch for momentary buttons
    switch(note) {
      case SHIFT_NOTE:
        if (noteOn) mShift = true;
        else mShift = false;
        updateTrackButtons();
        break;
      case TRACK_NUM_NOTE:
      case AB_NOTE:
        int rc_idx = note == TRACK_NUM_NOTE ? 0 : 1;
        int old_val = mPadControlsValue[channel];
        double new_val = 0;
        if (mShift && noteOn)
          new_val = (double)(old_val ^ (1 << rc_idx)) / 3;
        else if (noteOn)
          new_val = (double)(old_val | (1 << rc_idx)) / 3;
        else if (!mShift)
          new_val = (double)(old_val & ~(1 << rc_idx)) / 3;
        else
          return;
        mPadControls[channel].setImmediately(new_val);
        mPadControlsValue[channel] = (int)Math.round(new_val * 3);
        break;
    }

    if (velocity == 0) return;
    if (!noteOn) return;

    if (note < NUM_TRACKS * NUM_SCENES) {
      ClipLauncherSlot slot = mChannels[noteToCol(note)].clipLauncherSlotBank().getItemAt(noteToRow(note));
      slot.launch();
      return;
    }

    if (note >= SCENE_NOTE && note < SCENE_NOTE + NUM_SCENES) {
      mSceneBank.launch(note - SCENE_NOTE);
      return;
    }

    // switch for noteOn only buttons
    switch(note) {
      case CLIP_DEV_VIEW_NOTE:
        mApplication.toggleDevices();
        break;
      case DETAIL_VIEW_NOTE:
        mCursorDevice.isWindowOpen().toggle();
        break;
      case DEVICE_ON_OFF_NOTE:
        mCursorDevice.isEnabled().toggle();
        break;
      case BANK_LEFT_NOTE:
        mEditorRemoteControls.selectPreviousPage(true);
        break;
      case BANK_RIGHT_NOTE:
        mEditorRemoteControls.selectNextPage(true);
        break;
      case DEVICE_LEFT_NOTE:
        mCursorDevice.selectPrevious();
        break;
      case DEVICE_RIGHT_NOTE:
        mCursorDevice.selectNext();
        break;
      case TAP_TEMPO_NOTE:
        mTransport.tapTempo();
        break;
      case METRONOME_NOTE:
        mTransport.isMetronomeEnabled().toggle();
        break;
      case PLAY_NOTE:
        mTransport.togglePlay();
        break;
      case RECORD_NOTE:
        mTransport.isArrangerRecordEnabled().toggle();
        break;
      case SESSION_REC_NOTE:
        mTransport.isArrangerAutomationWriteEnabled().toggle();
        break;
      case MASTER_NOTE:
        mMasterTrack.selectInEditor();
        break;
      case STOP_ALL_NOTE:
        for (int col = 0; col < NUM_TRACKS; col++)
        mChannels[col].stop();
        break;
      case CLIP_STOP_NOTE:
        mChannels[channel].stop();
        break;
      case UP_NOTE:
        mSceneBank.scrollPageBackwards();
        mSceneBank.getItemAt(NUM_SCENES - 1).showInEditor();
        mSceneBank.getItemAt(0).showInEditor();
        break;
      case DOWN_NOTE:
        mSceneBank.scrollPageForwards();
        mSceneBank.getItemAt(0).showInEditor();
        mSceneBank.getItemAt(NUM_SCENES - 1).showInEditor();
        break;
      case LEFT_NOTE:
        mTrackBank.scrollBackwards();
        mTrackBank.getItemAt(7).makeVisibleInMixer();
        break;
      case RIGHT_NOTE:
        mTrackBank.scrollForwards();
        mTrackBank.getItemAt(0).makeVisibleInMixer();
        break;
      case SELECT_NOTE:
        mChannels[channel].selectInEditor();
        break;
      case SOLO_NOTE:
        boolean solo_val = mChannels[channel].solo().getAsBoolean();
        mChannels[channel].solo().set(!solo_val);
        break;
      case ARM_NOTE:
        if (!mShift) {
          boolean rec_val = mChannels[channel].arm().getAsBoolean();
          mChannels[channel].arm().set(!rec_val);
        }
        else {
          boolean mute_val = mChannels[channel].mute().getAsBoolean();
          mChannels[channel].mute().set(!mute_val);
        }
        break;
      case PAN_NOTE:
        mTopKnobControl = TOP_KNOBS_DEVICE;
        updateAllKnobLEDs();
        updateTopKnobSelector();
        break;
      case SEND_A_NOTE:
        mTopKnobControl = TOP_KNOBS_SEND1;
        updateAllKnobLEDs();
        updateTopKnobSelector();
        break;
      case SEND_B_NOTE:
        mTopKnobControl = TOP_KNOBS_SEND2;
        updateAllKnobLEDs();
        updateTopKnobSelector();
        break;
      case BANK_NOTE:
        mSideKnobControl = mSideKnobControl == SIDE_KNOBS_DEVICE ? SIDE_KNOBS_GLOBAL : SIDE_KNOBS_DEVICE;
        updateAllKnobLEDs();
        updateKnobIndicators();
        updateSideButtons();
        break;
    }
  }

  private void updateKnobLEDs(int bank, int idx, int value) {
    if (bank == mTopKnobControl)
      mMidiOut.sendMidi((MSG_CC << 4) | 0, TOP_KNOBS_CC + idx, value);
    else if (bank == mSideKnobControl)
      mMidiOut.sendMidi((MSG_CC << 4) | 0, SIDE_KNOBS_CC + idx, value);
  }

  private void updateAllKnobLEDs() {
    for (int idx = 0; idx < 8; idx++) {
      // top row
      int value = (int)(mKnobControls[mTopKnobControl][idx].get() * 127);
      mMidiOut.sendMidi((MSG_CC << 4) | 0, TOP_KNOBS_CC + idx, value);
      mMidiOut.sendMidi((MSG_CC << 4) | 0, TOP_KNOBS_CC + 8 + idx, mKnobTypes[mTopKnobControl][idx]);
      // side knobs
      value = (int)(mKnobControls[mSideKnobControl][idx].get() * 127);
      mMidiOut.sendMidi((MSG_CC << 4) | 0, SIDE_KNOBS_CC + idx, value);
      mMidiOut.sendMidi((MSG_CC << 4) | 0, SIDE_KNOBS_CC + 8 + idx, mKnobTypes[mSideKnobControl][idx]);
    }
  }

  private int mFaderPoints[][] = {
    {0, 0},
    {6, 10},
    {23, 25},
    {37, 40},
    {50, 51},
    {70, 64},
    {88, 80},
    {108, 90},
    {127, 100},
  };

  private int scaleFader(int cc) {
    int ret_val = 0;
    for (int i = 1; i < mFaderPoints.length; i++) {
      if (cc <= mFaderPoints[i][0]) {
        float x1 = mFaderPoints[i-1][0];
        float y1 = mFaderPoints[i-1][1];
        float x2 = mFaderPoints[i][0];
        float y2 = mFaderPoints[i][1];

        float slope = (y2 - y1) / (x2 - x1);
        float b = y1 - slope * x1;

        ret_val = (int)(slope * (float)cc + b);

        // mHost.println(ret_val + " = " + slope + "*" + cc + " + " + b);
        break;
      }
    }
    return ret_val;
  }

  private void processCC(int cc, int value, int channel) {
    if (cc == VOLUME_CC) {
      mChannels[channel].volume().set(scaleFader(value), 128);
    }
    else if (cc == TEMPO_CC) {
      if (value < 64) mTransport.tempo().incRaw(value);
      else mTransport.tempo().incRaw(value - 128);
    }
    else if (cc >= TOP_KNOBS_CC && cc < TOP_KNOBS_CC + 8) {
      int idx = cc - TOP_KNOBS_CC;
      mKnobControls[mTopKnobControl][idx].set(value, 128);
      mKnobAPCValues[mTopKnobControl][idx] = value;
    }
    else if (cc >= SIDE_KNOBS_CC && cc < SIDE_KNOBS_CC + 8) {
      int idx = cc - SIDE_KNOBS_CC;
      mKnobControls[mSideKnobControl][idx].set(value, 128);
      mKnobAPCValues[mSideKnobControl][idx] = value;
    }
  }

  private void onMidi(ShortMidiMessage msg) {
    final int code = msg.getStatusByte() & 0xF0;

    mHost.println("midi: " + msg.getStatusByte() + ", " + msg.getData1() + ", " + msg.getData2());

    switch (code) {
      // Note on/off
      case 0x80:
      case 0x90:
        processNote(msg.getData1(), msg.getData2(), msg.getChannel(), msg.isNoteOn());
        break;
      // CC
      case 0xB0:
        processCC(msg.getData1(), msg.getData2(), msg.getChannel());
        break;
      default:
        mHost.println("Unhandled midi status: " + msg.getStatusByte());
        break;
    }
  }
}
