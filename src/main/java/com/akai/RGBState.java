package com.akai;

import static com.akai.SimplerAPC40Extension.*;

import com.bitwig.extension.api.Color;
import com.bitwig.extension.controller.api.HardwareLightVisualState;
import com.bitwig.extension.controller.api.InternalHardwareLightState;
import com.bitwig.extension.controller.api.MidiOut;

public class RGBState extends InternalHardwareLightState {
   public static final RGBState OFF = new RGBState(0);
   public static final RGBState OFF_BLINK = new RGBState(0, 1);
   public static final RGBState DARKGREY = new RGBState(1);
   public static final RGBState DARKGREY_BLINK = new RGBState(1, 1);
   public static final RGBState GREY = new RGBState(2);
   public static final RGBState WHITE = new RGBState(3);
   public static final RGBState WHITE_BLINK = new RGBState(3, 1);
   public static final RGBState WHITE_PULSE = new RGBState(3, 2);
   public static final RGBState DARKRED = new RGBState(7);
   public static final RGBState DARKRED_BLINK = new RGBState(7, 1);
   public static final RGBState BLUE = new RGBState(79);
   public static final RGBState YELLOW = new RGBState(13);
   public static final RGBState YELLOW_BLINK = new RGBState(13, 1);
   public static final RGBState DARKYELLOW = new RGBState(15);
   public static final RGBState PURPLE = new RGBState(94);
   public static final RGBState ORANGE = new RGBState(9);
   public static final RGBState DARKORANGE = new RGBState(11);
   public static final RGBState RED = new RGBState(72);
   public static final RGBState RED_PULS = new RGBState(72, 2);
   public static final RGBState RED_BLINK = new RGBState(5, 1); // 72 instead of 5
   public static final RGBState GREEN = new RGBState(21);
   public static final RGBState DARK_GREEN = new RGBState(123);
   public static final RGBState GREEN_PULS = new RGBState(21, 2);
   public static final RGBState GREEN_BLINK = new RGBState(21, 1);

   public static final int RGB_TYPE_STATIC = 0;
   public static final int RGB_TYPE_PULSING = 10;
   public static final int RGB_TYPE_PULSING_FAST = 8;
   public static final int RGB_TYPE_BLINKING = 13;

   // TO DO find problematic colors!

   public RGBState(int n) {
      mNumber = n;
      mColor = Color.fromHex((String)RGB_HEX_COLOR_TABLE[n][0]);
   }

   public RGBState(int n, int t) {
      mNumber = n;
      mColor = Color.fromHex((String)RGB_HEX_COLOR_TABLE[n][0]);
      mType = t;
   }

   public RGBState(Color c) {
      double bestErr = 9999.;
      int best = 0;
      double err;
      for (int i = 0; i < RGB_HEX_COLOR_TABLE.length; i++) {
         // err = computeHsvError(c, Color.fromHex(RGB_HEX_COLOR_TABLE[i][0]));
         err = RGBError(c, Color.fromHex((String)RGB_HEX_COLOR_TABLE[i][0]));
         // mHost.println("error of " + c.toHex() + " and " + RGB_HEX_COLOR_TABLE[i][0] + " is " + err);
         if (err < bestErr) {
            best = (int)RGB_HEX_COLOR_TABLE[i][1];
            bestErr = err;
            if (err == 0) break;
         }
      }
      mNumber = best;
      mHost.println("best: " + mNumber);
      // mHost.println("input: " + c.toHex());
      mHost.println("chosen hex: " + Color.fromHex((String)RGB_HEX_COLOR_TABLE[best][0]).toHex());

      //mColor = Color.fromHex(Integer.toHexString(RGB_COLOR_TABLE[best][1]));
      mColor = c; // When sending via Sysex
   }

   public RGBState(Color c, int t) {
      double bestErr = 9999.;
      int best = 0;
      double err;
      for (int i = 0; i < RGB_HEX_COLOR_TABLE.length; i++) {
         err = computeHsvError(c, Color.fromHex((String)RGB_HEX_COLOR_TABLE[i][0]));
         // err = RGBError(c, Color.fromHex((String)RGB_HEX_COLOR_TABLE[i][0]));
         if (err < bestErr) {
            best = (int)RGB_HEX_COLOR_TABLE[i][1];
            bestErr = err;
            if (err == 0) break;
         }
      }
      mNumber = best;
      mColor = Color.fromHex((String)RGB_HEX_COLOR_TABLE[best][0]);
      mType = t;
   }

   @Override
   public HardwareLightVisualState getVisualState() {
      return HardwareLightVisualState.createForColor(this.getColor());
   }

   public int getMessage() {
      return mNumber;
   }

   public Color getColor() {
      return mColor;
   }

   public int getType() {
      return mType;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      final RGBState other = (RGBState) obj;
      return mNumber == other.mNumber && mType == other.mType;
   }

   public static void send(MidiOut m, int i, RGBState s) {
      if (s.mType == RGB_TYPE_PULSING || s.mType == RGB_TYPE_PULSING_FAST) {
         m.sendMidi((MSG_NOTE_ON << 4), i, s.mNumber);
         m.sendMidi((MSG_NOTE_ON << 4) | s.mType, i, WHITE.mNumber);
      }
      else if (s.mType == RGB_TYPE_BLINKING) {
         m.sendMidi((MSG_NOTE_ON << 4), i, s.mNumber);
         m.sendMidi((MSG_NOTE_ON << 4) | s.mType, i, WHITE.mNumber);
      }
      else {
         m.sendMidi((MSG_NOTE_ON << 4), i, s.mNumber);
      }
   }  

   public static void sendSys(final MidiOut m, int i, RGBState s) {
      send(m, i, s);
   }

   private int mNumber;
   private Color mColor;
   private int mType = 0;

   public static void RGBtoHSV(final double r, final double g, final double b, final double[] hsv) {
      assert r >= 0 && r <= 1;
      assert g >= 0 && g <= 1;
      assert b >= 0 && b <= 1;
      assert hsv != null;
      assert hsv.length == 3;

      double min, max, delta;
      double h, s, v;

      min = Math.min(Math.min(r, g), b);
      max = Math.max(Math.max(r, g), b);
      v = max; // v

      delta = max - min;

      if (max != 0) {
         s = delta / max; // s
      } else {
         // r = g = b = 0 // s = 0, v is undefined
         s = 0;
         h = 0;
         assert h >= 0 && h <= 360;
         assert s >= 0 && s <= 1;
         assert v >= 0 && v <= 1;

         hsv[0] = h;
         hsv[1] = s;
         hsv[2] = v;
         return;
      }

      if (delta == 0) {
         h = 0;
      } else {
         if (r == max) {
            h = (g - b) / delta; // between yellow & magenta
         } else if (g == max) {
            h = 2 + (b - r) / delta; // between cyan & yellow
         } else {
            h = 4 + (r - g) / delta; // between magenta & cyan
         }
      }

      h *= 60; // degrees
      if (h < 0) {
         h += 360;
      }

      assert h >= 0 && h <= 360;
      assert s >= 0 && s <= 1;
      assert v >= 0 && v <= 1;

      hsv[0] = h;
      hsv[1] = s;
      hsv[2] = v;
   }

   private static double computeHsvError(Color c, final Color color) {
      double[] hsv = new double[3];
      RGBtoHSV(c.getRed(), c.getGreen(), c.getBlue(), hsv);
      double[] hsvRef = new double[3];
      RGBtoHSV(color.getRed(), color.getGreen(), color.getBlue(), hsvRef);

      double hueError = (hsv[0] - hsvRef[0]) / 30;
      double sError = (hsv[1] - hsvRef[1]) * 1.6f;
      final double vScale = 1f;
      double vError = (vScale * hsv[2] - hsvRef[2]) / 40;

      final double error = hueError * hueError + vError * vError + sError * sError;

      return error;
   }

   private static double RGBError(Color c, Color r) {
      double RedMean = 0.5*(c.getRed255() + r.getRed255());
      double RedErr = (2 + RedMean/256) * Math.pow(c.getRed() - r.getRed(), 2);
      double GreenErr = 4 * Math.pow(c.getGreen() - r.getGreen(), 2);
      double BlueErr = (2  + (255 - RedMean)/256) * Math.pow(c.getBlue() - r.getBlue(), 2);
      return Math.sqrt(RedErr + GreenErr + BlueErr);
   }

   private Object[][] RGB_HEX_COLOR_TABLE = {
      // Kelvin palette manual map

      {"ff6393", 57}, // 9,1
      {"ff1800", 120}, // 9,2
      {"db1200", 6}, // 9,3
      
      {"ff8593", 4}, // 8,1
      {"ff4a00", 60}, // 8,2
      {"db3600", 127}, // 8,3

      {"f4a893", 107}, // 7,1
      {"f27e00", 84}, // 7,2
      {"d05d00", 10}, // 7,3

      {"ffd293", 12}, // 6,1
      {"ffbc00", 97}, // 6,2
      {"db8a00", 61}, // 6,3

      {"f6f69d", 109}, // 5,1
      {"ffff26", 13}, // 5,2
      {"dbbc1c", 14}, // 5,3

      {"6ffeff", 32}, // 4,1
      {"4dfeff", 33}, // 4,2
      {"42bbbc", 34}, // 4,3

      {"5ad9ff", 37}, // 3,1
      {"33c6ff", 38}, // 3,2
      {"2c91bc", 39}, // 3,3

      {"45affc", 41}, // 2,1
      {"1988f9", 42}, // 2,2
      {"1564b7", 43}, // 2,3

      {"458bff", 66}, // 1,1
      {"1953ff", 45}, // 1,2
      {"153dbc", 46}, // 1,3

      {"d9ffb3", 16}, // light green

      {"000000", 0},
      {"1E1E1E", 1},
      {"7F7F7F", 2},
      {"FFFFFF", 3},
      {"FF4C4C", 4},
      {"FF0000", 5},
      {"590000", 6},
      {"190000", 7},
      {"FFBD6C", 8},
      {"FF5400", 9},
      {"591D00", 10},
      {"271B00", 11},
      {"FFFF4C", 12},
      {"FFFF00", 13},
      {"595900", 14},
      {"191900", 15},
      {"88FF4C", 16},
      {"54FF00", 17},
      {"1D5900", 18},
      {"142B00", 19},
      {"4CFF4C", 20},
      {"00FF00", 21},
      {"005900", 22},
      {"001900", 23},
      {"4CFF5E", 24},
      {"00FF19", 25},
      {"00590D", 26},
      {"001902", 27},
      {"4CFF88", 28},
      {"00FF55", 29},
      {"00591D", 30},
      {"001F12", 31},
      {"4CFFB7", 32},
      {"00FF99", 33},
      {"005935", 34},
      {"001912", 35},
      {"4CC3FF", 36},
      {"00A9FF", 37},
      {"004152", 38},
      {"001019", 39},
      {"4C88FF", 40},
      {"0055FF", 41},
      {"001D59", 42},
      {"000819", 43},
      {"4C4CFF", 44},
      {"0000FF", 45},
      {"000059", 46},
      {"000019", 47},
      {"874CFF", 48},
      {"5400FF", 49},
      {"190064", 50},
      {"0F0030", 51},
      {"FF4CFF", 52},
      {"FF00FF", 53},
      {"590059", 54},
      {"190019", 55},
      {"FF4C87", 56},
      {"FF0054", 57},
      {"59001D", 58},
      {"220013", 59},
      {"FF1500", 60},
      {"993500", 61},
      {"795100", 62},
      {"436400", 63},
      {"033900", 64},
      {"005735", 65},
      {"00547F", 66},
      {"0000FF", 67},
      {"00454F", 68},
      {"2500CC", 69},
      {"7F7F7F", 70},
      {"202020", 71},
      {"FF0000", 72},
      {"BDFF2D", 73},
      {"AFED06", 74},
      {"64FF09", 75},
      {"108B00", 76},
      {"00FF87", 77},
      {"00A9FF", 78},
      {"002AFF", 79},
      {"3F00FF", 80},
      {"7A00FF", 81},
      {"B21A7D", 82},
      {"402100", 83},
      {"FF4A00", 84},
      {"88E106", 85},
      {"72FF15", 86},
      {"00FF00", 87},
      {"3BFF26", 88},
      {"59FF71", 89},
      {"38FFCC", 90},
      {"5B8AFF", 91},
      {"3151C6", 92},
      {"877FE9", 93},
      {"D31DFF", 94},
      {"FF005D", 95},
      {"FF7F00", 96},
      {"B9B000", 97},
      {"90FF00", 98},
      {"835D07", 99},
      {"392b00", 100},
      {"144C10", 101},
      {"0D5038", 102},
      {"15152A", 103},
      {"16205A", 104},
      {"693C1C", 105},
      {"A8000A", 106},
      {"DE513D", 107},
      {"D86A1C", 108},
      {"FFE126", 109},
      {"9EE12F", 110},
      {"67B50F", 111},
      {"1E1E30", 112},
      {"DCFF6B", 113},
      {"80FFBD", 114},
      {"9A99FF", 115},
      {"8E66FF", 116},
      {"404040", 117},
      {"757575", 118},
      {"E0FFFF", 119},
      {"A00000", 120},
      {"350000", 121},
      {"1AD000", 122},
      {"074200", 123},
      {"B9B000", 124},
      {"3F3100", 125},
      {"B35F00", 126},
      {"4B1502", 127}
   };
}