package com.akai;

import com.bitwig.extension.controller.api.*;
import com.bitwig.extension.controller.ControllerExtension;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SimplerAPC40Extension extends ControllerExtension
{
   static final int NUM_SCENES = 5;
   static final int NUM_TRACKS = 8;
   static final int NUM_SENDS = 8;
   static final int HOLD_DELAY = 5;

   static final int MSG_NOTE_ON = 9;
   static final int MSG_NOTE_OFF = 8;
   static final int MSG_CC = 11;

   protected SimplerAPC40Extension(final SimplerAPC40ExtensionDefinition definition, final ControllerHost host) {
      super(definition, host);
   }

   @Override
   public void init() {
      mHost = getHost();
      
      mTransport = mHost.createTransport();
      mTransport.isPlaying().markInterested();

      mTrackBank = mHost.createTrackBank(NUM_TRACKS, NUM_SENDS, NUM_SCENES);

      mSceneBank = mTrackBank.sceneBank();
      mTrackBank.setShouldShowClipLauncherFeedback(true);

      mMidiIn = mHost.getMidiInPort(0);
      mMidiOut = mHost.getMidiOutPort(0);

      mRemoteControls = new CursorRemoteControlsPage[NUM_TRACKS];
      mEditorRemoteControls = mHost.createCursorTrack(3, NUM_SCENES).createCursorDevice().createCursorRemoteControlsPage(8);
      mMasterRemotes = mHost.createMasterTrack(0).createCursorRemoteControlsPage(8);
      
      mCursorClip = mHost.createLauncherCursorClip(1, 1);
      mCursorClip.clipLauncherSlot().isSelected().markInterested();
      mCursorClip.clipLauncherSlot().sceneIndex().addValueObserver(idx -> {
         if (mCursorClip.clipLauncherSlot().isSelected().get())
            mSceneBank.scrollPosition().set((idx / NUM_SCENES) * NUM_SCENES);
      });

      for (int i = 0; i < 8; i++) {
         mEditorRemoteControls.getParameter(i).markInterested();
         mMasterRemotes.getParameter(i).markInterested();
      }

      for (int col = 0; col < NUM_TRACKS; col++) {
         final int trackIdx = col;
         final Track track = mTrackBank.getItemAt(trackIdx);
         mRemoteControls[col] = track.createCursorRemoteControlsPage(8);

         for (int i = 0; i < 8; i++) mRemoteControls[col].getParameter(i).markInterested();
         for (int i = 0; i < NUM_SENDS; i++) track.sendBank().getItemAt(i).markInterested();

         track.isStopped().markInterested();
         track.isQueuedForStop().markInterested();
         track.color().markInterested();
         track.mute().markInterested();
         track.solo().markInterested();
         track.arm().markInterested();
         final ClipLauncherSlotBank slotBank = track.clipLauncherSlotBank();
         for (int row = 0; row < NUM_SCENES; row++) {
            ClipLauncherSlot slot = slotBank.getItemAt(row);
            slot.color().markInterested();
            slot.isPlaying().markInterested();
            slot.hasContent().markInterested();
            slot.isPlaybackQueued().markInterested();
            slot.isStopQueued().markInterested();
            slot.isRecordingQueued().markInterested();
            slot.isRecording().markInterested();
         }
      }

      mAPC40 = new APC40();
      mAPC40.init();

      Runnable timerRunnable = new Runnable() {
         public void run() {
            if (mSceneUpHeld) mSceneUpTimer++;
            else mSceneUpTimer = 0;

            if (mSceneDownHeld) mSceneDownTimer++;
            else mSceneDownTimer = 0;

            if (mSceneUpTimer > HOLD_DELAY) {
               mSceneBank.scrollPageBackwards();
               mSceneBank.getItemAt(NUM_SCENES - 1).showInEditor();
               mSceneBank.getItemAt(0).showInEditor();
            }
            if (mSceneDownTimer > HOLD_DELAY) {
               mSceneBank.scrollPageForwards();
               mSceneBank.getItemAt(0).showInEditor();
               mSceneBank.getItemAt(NUM_SCENES - 1).showInEditor();
            }
         }
      };
     
      ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
      executor.scheduleAtFixedRate(timerRunnable, 0, 100, TimeUnit.MILLISECONDS);

      mHost.showPopupNotification("APC40 Mixlaunch Initialized");
   }

   @Override
   public void exit() {
      mAPC40.exit();
      mHost.showPopupNotification("APC40 Mixlaunch Exited");
   }

   @Override
   public void flush() {
   }

   public APC40 mAPC40;
   
   public static ControllerHost mHost;
   public static Transport mTransport;

   public static MidiOut mMidiOut;
   public static MidiIn mMidiIn;

   public static TrackBank mTrackBank;
   public static SceneBank mSceneBank;
   
   public static CursorRemoteControlsPage mMasterRemotes;
   public static CursorRemoteControlsPage[] mRemoteControls;
   public static CursorRemoteControlsPage mEditorRemoteControls;
   public static Clip mCursorClip;

   public static boolean mSceneUpHeld = false;
   public static int mSceneUpTimer = 0;
   public static boolean mSceneDownHeld = false;
   public static int mSceneDownTimer = 0;

   public static boolean mShift;
}