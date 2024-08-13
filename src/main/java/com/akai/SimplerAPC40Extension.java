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
      mApplication = mHost.createApplication();
      
      mTransport = mHost.createTransport();
      mTransport.isPlaying().markInterested();

      mTrackBank = mHost.createTrackBank(NUM_TRACKS, NUM_SENDS, NUM_SCENES);
      mMasterTrack = mHost.createMasterTrack(NUM_SCENES);
      mSceneBank = mTrackBank.sceneBank();
      mSceneBank.setIndication(true);
      mTrackBank.setShouldShowClipLauncherFeedback(true);

      mAPCMidiIn = mHost.getMidiInPort(0);
      mAPCMidiOut = mHost.getMidiOutPort(0);
      mControlMidiIn = mHost.getMidiInPort(1);
      mControlMidiOut = mHost.getMidiOutPort(1);

      mRemoteControls = new CursorRemoteControlsPage[NUM_TRACKS];
      mCursorDevice = mHost.createCursorTrack(3, NUM_SCENES).createCursorDevice();
      mEditorRemoteControls = mCursorDevice.createCursorRemoteControlsPage(8);
      mMasterRemotes = mHost.createMasterTrack(0).createCursorRemoteControlsPage(8);
      mUserControlBank = mHost.createUserControls(8);
      
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

      mLaunchControlXL = new LaunchControlXL();
      mLaunchControlXL.init();

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
   public LaunchControlXL mLaunchControlXL;
   
   public static ControllerHost mHost;
   public static Application mApplication;
   public static Transport mTransport;

   public static MidiOut mAPCMidiOut;
   public static MidiOut mControlMidiOut;
   public static MidiIn mAPCMidiIn;
   public static MidiIn mControlMidiIn;

   public static TrackBank mTrackBank;
   public static SceneBank mSceneBank;
   public static MasterTrack mMasterTrack;
   
   public static CursorRemoteControlsPage mMasterRemotes;
   public static CursorRemoteControlsPage[] mRemoteControls;
   public static CursorRemoteControlsPage mEditorRemoteControls;
   public static UserControlBank mUserControlBank;
   public static CursorDevice mCursorDevice;
   public static Clip mCursorClip;

   public static boolean mShift;
}