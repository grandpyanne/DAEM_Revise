"""
Blueprint sketches for Kapi's topic screens and the labelled diagrams in the notes.

Each drawing is written in a tiny drawing language on a 200-wide grid (y grows downward),
commands separated by ';'. This script writes them into
app/src/main/java/com/grandpyanne/daemrevise/ui/TopicSketches.kt.

  L x1 y1 x2 y2        line            l ... thin line       W ... thick line    - ... dashed line
  A x1 y1 x2 y2        arrow (head at x2,y2)                  D x1 y1 x2 y2       dimension (both ends)
  R x y w h            rectangle       r x y w h  dashed rectangle
  O cx cy r            circle          o cx cy r  dashed circle     F cx cy r  filled dot
  E cx cy rx ry        ellipse
  a cx cy r start sweep  arc (degrees, clockwise from +x)
  P x y x y ...        open polyline   G x y ...  closed polygon    H x y ...  shaded polygon
  B x0 y0 c1x c1y c2x c2y x y   curve   b ... dashed curve
  Z x0 y0 x1 y1 n      zigzag (resistor/spring) with n points
  K x0 y x1 n          coil (inductor) along y from x0 to x1, n loops
  g x y                earth symbol
  T x y size text      label, centred       t x y size text   label, left-aligned
Crafted by Grandpyanne(TM).
"""
import json, math, os, sys

def f(v): return ("%.1f" % v).rstrip("0").rstrip(".")
def j(*parts): return ";".join(p for p in parts if p)

# ---------- topic sketches (no text, or very little) ----------
S = {}

# Mech Tech
S["mt-1"] = j("L 20 180 190 180", "L 20 180 20 15", "P 20 180 70 60 78 57 88 66 98 62",
              "B 98 62 130 26 165 26 186 60", "F 186 60 2.5", "- 70 60 70 180", "- 20 60 70 60",
              "A 120 120 72 80")
S["mt-2"] = j("R 40 80 120 30", "A 40 95 5 95", "A 160 95 195 95", "- 160 66 160 130", "- 172 66 172 130",
              "D 160 124 172 124", "l 40 80 40 60", "l 160 80 160 60", "D 40 55 160 55", "l 70 80 70 110", "l 130 80 130 110")
S["mt-3"] = j("R 30 60 140 80", "R 30 85 140 30", "- 30 100 170 100", "A 100 18 100 55", "A 60 22 60 55", "A 140 22 140 55",
              "W 25 140 175 140", "l 30 145 40 152", "l 60 145 70 152", "l 90 145 100 152", "l 120 145 130 152", "l 150 145 160 152")
S["mt-4"] = j("E 50 100 18 50", "L 50 50 160 50", "L 50 150 160 150", "E 160 100 18 50", "A 50 100 5 100", "A 160 100 196 100",
              "A 105 50 105 18", "A 105 150 105 182", "D 135 50 135 150", "- 50 100 160 100")
S["mt-5"] = j("E 50 100 12 32", "L 50 68 150 68", "L 50 132 150 132", "E 150 100 12 32", "l 50 74 150 96", "- 20 100 180 100",
              "a 50 100 40 200 130", "A 30 84 33 76", "a 150 100 40 -20 130", "A 170 116 167 124", "D 60 150 140 150")
S["mt-8"] = j("G 50 30 150 30 150 50 110 50 110 150 150 150 150 170 50 170 50 150 90 150 90 50 50 50",
              "- 20 100 180 100", "- 100 15 100 185", "D 168 30 168 100", "F 100 100 3")
S["mt-9"] = j("L 10 100 190 100", "R 46 70 8 60", "R 146 70 8 60", "R 96 75 8 50", "L 50 70 50 52", "F 50 46 9",
              "L 150 130 150 148", "F 150 154 9", "L 100 75 100 64", "F 100 58 6", "- 50 20 50 185", "- 150 20 150 185", "a 100 100 40 250 130", "A 74 74 70 79")
S["mt-11"] = j("W 10 100 70 100", "W 124 100 190 100", "R 70 40 12 120", "R 84 52 5 96", "R 93 52 5 96", "R 100 40 12 120",
               "A 20 62 66 62", "A 20 138 66 138", "a 155 100 26 -60 120", "A 160 125 152 127")
S["mt-12"] = j("O 55 100 40", "O 155 100 22", "L 53 60 153 78", "L 53 140 153 122", "O 55 100 5", "O 155 100 4",
               "A 92 67 118 72", "A 118 128 92 133", "a 55 100 50 140 80", "A 15 75 22 66")
S["mt-13"] = j("O 90 100 60", "O 90 100 50", "O 90 100 8", "L 90 50 90 150", "L 40 100 140 100", "L 55 65 125 135",
               "L 55 135 125 65", "A 150 100 188 60", "a 90 100 72 -70 55", "A 128 41 118 34")
S["mt-14"] = j("E 100 112 70 24", "E 100 96 70 24", "L 30 96 30 112", "L 170 96 170 112", "- 100 15 100 185", "F 100 96 3",
               "D 100 160 162 160", "a 100 38 18 200 140", "A 116 32 120 38", "F 150 96 3")

# Electronics
lat = []
for x in (40, 100, 160):
    for y in (40, 100, 160):
        lat.append(f"O {x} {y} 11")
for x in (40, 100, 160):
    lat += [f"l {x} 51 {x} 89", f"l {x} 111 {x} 149"]
for y in (40, 100, 160):
    lat += [f"l 51 {y} 89 {y}", f"l 111 {y} 149 {y}"]
lat += ["o 130 70 6", "F 75 130 3.5", "A 75 130 90 145"]
S["ec-1"] = j(*lat)
S["ec-2"] = j("O 100 100 34", "W 86 76 86 124", "L 50 100 86 100", "L 86 90 122 68", "L 86 110 122 132", "A 108 123 122 132",
              "L 122 68 122 46", "Z 122 46 122 18 6", "L 100 14 150 14", "L 122 132 122 170", "g 122 170", "l 50 100 50 80", "Z 50 80 50 40 6", "L 50 40 50 14")
S["ec-4"] = j("G 60 50 60 130 130 90", "l 64 70 72 70", "l 64 110 72 110", "l 68 106 68 114", "L 28 70 60 70", "L 28 110 60 110",
              "L 130 90 172 90", "L 45 70 45 30", "Z 45 30 150 30 7", "L 150 30 150 90",
              "P 20 172 40 172 40 148 70 148 70 172 100 172 100 148 130 148 130 172 160 172 160 148 190 148")
bits = []
for i, b in enumerate("10100101"):
    x = 20 + i * 20
    bits += [f"R {x} 80 20 30", f"T {x+10} 101 14 {b}"]
for i, w in enumerate([128, 64, 32, 16, 8, 4, 2, 1]):
    bits.append(f"T {30+i*20} 72 7 {w}")
bits += ["A 100 115 100 140", "T 100 162 16 = 165"]
S["ec-5"] = j(*bits)
S["ec-7"] = j("G 70 30 130 55 130 145 70 170", "L 30 50 70 50", "L 30 80 70 80", "L 30 110 70 110", "L 30 140 70 140",
              "L 130 100 172 100", "L 90 162 90 190", "L 110 154 110 190", "T 100 104 12 MUX", "F 30 50 2.5", "F 30 80 2.5", "F 30 110 2.5", "F 30 140 2.5")
S["ec-8"] = j("R 60 28 70 90", "L 28 50 60 50", "L 28 100 60 100", "P 60 92 68 100 60 108", "L 130 50 166 50", "L 134 100 166 100",
              "O 132 100 3", "T 70 54 11 D", "T 120 54 11 Q", "T 118 104 11 Q'",
              "P 20 172 40 172 40 146 60 146 60 172 80 172 80 146 100 146 100 172 120 172 120 146 140 146 140 172 160 172 160 146 180 146")
seg = ["W 42 30 78 30", "W 42 80 78 80", "W 42 130 78 130", "W 36 36 36 74", "W 84 36 84 74", "W 36 86 36 124", "W 84 86 84 124", "F 92 132 2.5"]
S["ec-9"] = j(*seg, "P 110 170 110 150 125 150 125 130 140 130 140 110 155 110 155 90 170 90 170 70 185 70", "- 110 175 190 60", "L 105 175 192 175", "L 108 178 108 62")
S["ec-10"] = j("A 8 80 38 80", "O 48 80 10", "l 41 73 55 87", "l 41 87 55 73", "A 58 80 85 80", "R 85 62 50 36", "A 135 80 192 80",
               "L 165 80 165 140", "A 165 140 128 140", "R 78 125 50 30", "L 78 140 48 140", "A 48 140 48 92",
               "T 110 85 14 G", "T 103 145 14 H", "T 34 72 9 +", "T 56 103 9 -")
S["ec-11"] = j("L 15 60 32 60", "Z 32 60 78 60 7", "K 82 60 132 4", "L 132 60 150 60", "L 150 60 150 92", "W 136 92 164 92", "W 136 100 164 100",
               "L 150 100 150 140", "g 150 140", "L 15 60 15 140", "l 15 140 150 140", "O 15 60 3", "O 15 140 3", "A 165 60 190 60", "T 55 50 9 R", "T 107 48 9 L", "T 174 98 9 C")
S["ec-12"] = j("L 22 20 22 92", "L 22 92 192 92", "P 22 38 92 38", "B 92 38 112 38 122 46 132 56", "L 132 56 190 88",
               "L 22 108 22 186", "L 22 186 192 186", "B 22 118 90 118 118 178 190 180", "- 22 160 192 160", "l 112 30 112 186")
S["ec-13"] = j("L 20 172 192 172", "L 20 172 20 18", "- 20 80 192 80", "P 20 172 20 80", "B 20 172 38 20 58 34 74 68",
               "B 74 68 88 100 100 92 114 79", "B 114 79 128 66 146 84 192 80", "D 180 80 180 172", "l 56 40 74 40", "D 66 40 66 80")

# Maths
S["mm-1"] = j("P 50 30 38 30 38 170 50 170", "P 150 30 162 30 162 170 150 170",
              "T 66 66 16 2", "T 100 66 16 0", "T 134 66 16 1",
              "T 66 106 16 3", "T 100 106 16 4", "T 134 106 16 2",
              "T 66 146 16 1", "T 100 146 16 5", "T 134 146 16 6", "- 56 52 146 154")
sinpts = " ".join(f"{f(20+x)} {f(100-55*math.sin(x/170*2*math.pi))}" for x in range(0, 171, 5))
t3 = " ".join(f"{f(20+x)} {f(100-55*((xx:=(x-85)/170*2*math.pi*-1)*-1 - (xx*-1)**3/6))}" for x in range(30, 141, 5))
S["mm-2"] = j("L 15 100 192 100", "L 105 18 105 182", "P " + sinpts, "- 50 160 160 40")
surf = []
for i in range(5):
    y0 = 60 + i * 20
    surf.append(f"B 30 {y0+30} 70 {y0-10+i*8} 130 {y0+40-i*8} 170 {y0}")
for i in range(5):
    x0 = 40 + i * 30
    surf.append(f"b {x0} 80 {x0+5} 110 {x0+10} 130 {x0+15} 170")
S["mm-3"] = j(*surf, "A 100 175 40 192", "A 100 175 188 186", "A 100 175 100 20")
field = []
for gx in range(30, 191, 22):
    for gy in range(30, 181, 22):
        yv = (190 - gy) / 160 * 3 - 1
        slope = -0.8 * yv
        ang = math.atan(slope)
        dx, dy = 7 * math.cos(ang), -7 * math.sin(ang)
        field.append(f"l {f(gx-dx)} {f(gy-dy)} {f(gx+dx)} {f(gy+dy)}")
curve = " ".join(f"{f(20+t)} {f(190 - (1.25 + 1.6*math.exp(-t/60)) / 3 * 160 - 0)}" for t in range(0, 171, 5))
S["mm-4"] = j(*field, "P " + curve, "L 15 190 192 190", "L 20 192 20 15")
damp = " ".join(f"{f(20+t)} {f(150 - 30*math.exp(-t/60)*math.cos(t/9))}" for t in range(0, 171, 2))
S["mm-5"] = j("W 10 20 10 80", "l 10 30 3 37", "l 10 50 3 57", "l 10 70 3 77", "Z 10 40 70 40 8", "R 70 26 40 40",
              "L 10 64 32 64", "R 32 58 22 12", "L 44 64 70 64", "L 40 60 40 68", "- 15 150 192 150", "P " + damp, "L 20 110 20 190")
S["mm-7"] = j("A 70 140 70 20", "A 70 140 190 160", "A 70 140 20 185", "A 70 140 160 110", "A 70 140 110 70", "l 160 110 200 40",
              "l 110 70 200 40", "F 70 140 3", "A 70 140 40 60", "T 168 104 10 a", "T 100 66 10 b", "T 34 54 10 a×b")
bell = " ".join(f"{f(20+x)} {f(170 - 130*math.exp(-((x-85)/30)**2/2))}" for x in range(0, 171, 3))
S["mm-8"] = j("L 15 170 192 170", "P " + bell, "- 105 40 105 170", "- 75 98 75 170", "- 135 98 135 170",
              "H " + " ".join(f"{f(20+x)} {f(170 - 130*math.exp(-((x-85)/30)**2/2))}" for x in range(55, 116, 3)) + " 135 170 75 170",
              "D 75 182 135 182", "R 150 20 30 30", "F 158 28 2.5", "F 172 42 2.5", "F 165 35 2.5")

# Propellers
S["pp-1"] = j("B 40 120 70 95 130 90 175 98", "B 175 98 130 106 80 118 40 120", "A 20 120 186 120", "A 108 186 108 112",
              "A 108 186 186 120", "l 40 120 175 98", "A 140 80 140 30", "A 160 110 192 104", "a 108 186 40 -90 50", "a 186 120 40 175 15",
              "T 150 26 9 thrust", "T 100 140 9 Vrot", "T 120 194 9 Vax")
S["pp-2"] = j("W 100 20 100 180", "L 100 60 60 100", "L 100 60 140 100", "F 60 100 10", "F 140 100 10", "R 85 110 30 14",
              "Z 100 124 100 165 7", "R 80 165 40 15", "A 60 100 40 100", "A 140 100 160 100", "a 100 40 18 200 140", "A 116 34 119 40")
blades = []
for i in range(3):
    a = math.radians(-90 + i * 120)
    x1, y1 = 100 + 20 * math.cos(a), 100 + 20 * math.sin(a)
    x2, y2 = 100 + 88 * math.cos(a), 100 + 88 * math.sin(a)
    nx, ny = -math.sin(a) * 12, math.cos(a) * 12
    blades.append(f"G {f(x1+nx*.6)} {f(y1+ny*.6)} {f(x2+nx*.5)} {f(y2+ny*.5)} {f(x2-nx*.5)} {f(y2-ny*.5)} {f(x1-nx*.6)} {f(y1-ny*.6)}")
    bx1, by1 = 100 + 30 * math.cos(a), 100 + 30 * math.sin(a)
    bx2, by2 = 100 + 60 * math.cos(a), 100 + 60 * math.sin(a)
    blades.append(f"- {f(bx1+nx*.3)} {f(by1+ny*.3)} {f(bx2+nx*.3)} {f(by2+ny*.3)}")
S["pp-3"] = j(*blades, "O 100 100 20", "F 100 100 4", "F 112 116 3", "a 100 100 98 -60 40")
bolts = [f"O {f(100+40*math.cos(math.radians(k*45)))} {f(100+40*math.sin(math.radians(k*45)))} 5" for k in range(8)]
S["pp-4"] = j("O 100 100 55", "O 100 100 20", *bolts, "- 30 100 170 100", "- 100 30 100 170", "D 100 172 155 172")
S["pp-5"] = j("G 50 20 130 20 150 40 150 180 50 180", "P 130 20 130 40 150 40", "L 66 60 134 60", "L 66 80 134 80", "L 66 100 120 100",
              "P 66 130 72 138 84 122", "L 92 130 134 130", "P 66 152 72 160 84 144", "L 92 152 134 152", "O 160 160 22", "o 160 160 16")

# Airfield Safety & Research
S["ar-1"] = j("B 20 100 30 80 60 76 150 82", "L 150 82 186 70", "L 186 70 190 92", "B 190 92 150 104 60 110 20 100",
              "G 80 92 110 92 104 120 86 120", "- 20 40 20 180", "A 20 150 100 150", "A 60 130 60 106", "A 130 130 130 104",
              "G 98 168 106 158 114 168", "T 30 36 9 datum", "T 106 182 9 CG")
S["ar-2"] = j("G 100 20 160 40 156 110 100 170 44 110 40 40", "R 78 88 44 36", "a 100 88 15 180 180", "F 100 102 4", "L 100 104 100 114")
rw = ["R 70 10 60 180"] + [f"R {76+i*8} 18 5 22" for i in range(6)] + [f"R {76+i*8} 160 5 22" for i in range(6)]
rw += [f"l 100 {y} 100 {y+12}" for y in range(52, 150, 20)] + ["T 100 58 12 09", "T 100 150 12 27"]
S["ar-3"] = j(*rw)
cal = ["R 30 40 140 130", "W 30 60 170 60", "l 64 34 64 46", "l 136 34 136 46"]
for c in range(1, 5):
    cal.append(f"l {30+c*28} 60 {30+c*28} 170")
for r in range(1, 4):
    cal.append(f"l 30 {60+r*27.5} 170 {60+r*27.5}")
cal += ["P 38 75 44 82 54 68", "P 66 102 72 110 82 96", "P 122 130 128 138 138 124", "O 150 158 8"]
S["ar-4"] = j(*cal)
S["ar-5"] = j("R 70 14 60 26", "R 140 70 54 26", "R 70 150 60 26", "R 6 70 54 26", "A 130 30 160 68", "A 165 96 132 150", "A 70 165 36 98", "A 36 70 68 30", "T 100 108 12 ?")
dots = []
import random
rnd = random.Random(4)
for _ in range(40):
    x, y = rnd.uniform(30, 170), rnd.uniform(40, 160)
    if ((x-100)/72)**2 + ((y-100)/62)**2 < 1:
        dots.append(f"F {f(x)} {f(y)} 2.6")
S["ar-6"] = j("E 100 100 75 64", *dots, "o 130 120 26", "A 160 140 186 170")
S["ar-7"] = j("L 20 170 120 170", "L 20 170 20 40", "R 30 120 16 50", "R 54 90 16 80", "R 78 60 16 110", "R 102 100 16 70",
              "P 38 110 62 84 86 52 110 92", "O 160 70 28", "L 160 70 160 42", "L 160 70 184 84", "L 160 70 140 92")

# Airframe
S["as-2"] = j("O 40 60 20", "G 32 72 48 72 40 50", "L 60 60 90 60", "R 90 46 70 28", "a 90 60 14 90 180", "a 160 60 14 -90 180",
              "L 175 60 192 60", "L 125 74 125 110", "R 112 110 26 18", "L 125 128 125 150", "O 125 165 15", "L 125 165 134 156", "F 125 165 2")
S["as-3"] = j("R 80 14 40 70", "R 88 84 24 50", "W 100 84 100 20", "O 100 158 28", "O 100 158 10", "P 80 84 66 100 88 120",
              "- 100 20 100 192", "l 84 60 116 60")
S["as-4"] = j("O 100 100 72", "O 100 100 52", "O 100 100 14", "R 150 72 30 56", "o 100 100 34", "l 100 28 100 48", "l 172 100 152 100",
              "l 28 100 48 100", "l 100 152 100 172")
S["as-5"] = j("O 100 100 74", "L 30 120 170 120", "A 50 80 80 80", "A 150 80 120 80", "A 100 60 100 30", "R 160 30 26 14",
              "A 173 44 173 70", "T 100 140 9 cabin", "o 100 100 60")
S["as-6"] = j("W 20 30 180 30", "L 40 30 40 60", "O 40 80 20", "T 40 85 14 G", "L 100 30 100 70", "W 88 70 112 70", "l 92 78 108 78",
              "W 88 86 112 86", "l 92 94 108 94", "L 100 94 100 120", "g 100 120", "L 160 30 160 60", "R 152 60 16 10", "L 160 70 160 100",
              "O 160 115 15", "l 150 105 170 125", "l 150 125 170 105")

# Theory of Flight
S["tf-1"] = j("B 20 100 40 84 100 82 160 90", "L 160 90 186 76", "L 186 76 190 100", "B 190 100 150 110 80 114 20 100",
              "G 92 98 120 98 110 140 98 140", "- 10 98 194 98", "- 104 30 104 180", "A 104 98 150 60", "F 104 98 3",
              "a 104 98 30 180 70", "a 104 98 30 260 60", "A 74 98 78 86")
S["tf-3"] = j("L 22 18 22 178", "L 22 178 192 178", "B 30 40 60 160 120 160 190 60", "F 92 132 3", "- 20 182 170 50",
              "- 92 132 92 178", "l 30 160 190 130", "l 30 60 190 170")
S["tf-4"] = j("L 22 18 22 182", "L 16 120 194 120", "B 22 120 60 120 80 60 100 40", "L 100 40 180 40", "L 180 40 180 120",
              "L 180 120 180 150", "B 22 120 60 120 80 140 100 150", "L 100 150 180 150", "- 22 40 100 40", "- 22 150 100 150", "T 30 32 9 n+", "T 186 136 9 Vd")
S["tf-5"] = j("W 100 60 100 100", "E 100 60 92 10", "L 10 60 190 60", "R 70 100 60 40", "P 70 120 30 120 20 112", "A 70 80 70 118",
              "A 100 80 100 120", "A 130 80 130 118", "A 100 170 100 192", "a 100 40 30 200 140", "A 126 30 130 36")

# ---------- labelled diagrams for the notes ----------
# id -> (height, title, drawing)
FIG = {}

gates = []
def gate(x, y, kind, label):
    out = []
    w = 34; h = 26
    if kind in ("and", "nand"):
        out += [f"P {x+w*0.5} {y} {x} {y} {x} {y+h} {x+w*0.5} {y+h}", f"a {x+w*0.5} {y+h/2} {h/2} -90 180"]
        tip = x + w*0.5 + h/2
    elif kind in ("or", "nor", "xor", "xnor"):
        out += [f"B {x} {y} {x+8} {y+h/2} {x+8} {y+h/2} {x} {y+h}",
                f"B {x} {y} {x+22} {y} {x+32} {y+6} {x+38} {y+h/2}",
                f"B {x} {y+h} {x+22} {y+h} {x+32} {y+h-6} {x+38} {y+h/2}"]
        if kind in ("xor", "xnor"):
            out.append(f"B {x-5} {y} {x+3} {y+h/2} {x+3} {y+h/2} {x-5} {y+h}")
        tip = x + 38
    else:  # not
        out.append(f"G {x} {y} {x} {y+h} {x+28} {y+h/2}")
        tip = x + 28
    if kind in ("nand", "nor", "xnor", "not"):
        out.append(f"O {tip+3} {y+h/2} 3")
        tip += 6
    if kind == "not":
        out.append(f"L {x-10} {y+h/2} {x} {y+h/2}")
    else:
        out += [f"L {x-10} {y+7} {x+(3 if kind in ('or','nor','xor','xnor') else 0)} {y+7}",
                f"L {x-10} {y+h-7} {x+(3 if kind in ('or','nor','xor','xnor') else 0)} {y+h-7}"]
    out.append(f"L {tip} {y+h/2} {tip+10} {y+h/2}")
    out.append(f"T {x+17} {y+h+13} 9 {label}")
    return out
gl = [(18, 14, "and", "AND  Y=A·B"), (82, 14, "or", "OR  Y=A+B"), (148, 14, "not", "NOT  Y=A'"),
      (18, 74, "nand", "NAND  (A·B)'"), (82, 74, "nor", "NOR  (A+B)'"), (148, 74, "xor", "XOR  A⊕B"),
      (82, 134, "xnor", "XNOR  (A⊕B)'")]
for x, y, k, lab in gl:
    gates += gate(x, y, k, lab)
gates.append("T 100 190 7 NAND and NOR are universal gates")
FIG["gates"] = (198, "The seven logic gates", j(*gates))

FIG["bjt"] = (120, "BJT symbols: the arrow is on the emitter and points the way conventional current flows", j(
    "O 55 60 32", "W 42 38 42 82", "L 10 60 42 60", "L 42 50 72 32", "L 42 70 72 88", "A 60 80 72 88", "L 72 32 72 10", "L 72 88 72 112",
    "T 8 54 9 B", "T 80 14 9 C", "T 80 110 9 E", "T 55 104 10 NPN",
    "O 150 60 32", "W 137 38 137 82", "L 105 60 137 60", "L 137 50 167 32", "L 137 70 167 88", "A 160 84 146 75", "L 167 32 167 10", "L 167 88 167 112",
    "T 103 54 9 B", "T 175 14 9 C", "T 175 110 9 E", "T 150 104 10 PNP"))

def diode(x, y, extra):
    out = [f"L {x} {y} {x+16} {y}", f"G {x+16} {y-10} {x+16} {y+10} {x+34} {y}", f"L {x+34} {y-10} {x+34} {y+10}", f"L {x+34} {y} {x+50} {y}"]
    if extra == "zener":
        out += [f"L {x+34} {y-10} {x+29} {y-14}", f"L {x+34} {y+10} {x+39} {y+14}"]
    if extra == "led":
        out += [f"A {x+24} {y-12} {x+34} {y-24}", f"A {x+30} {y-10} {x+40} {y-22}"]
    if extra == "photo":
        out += [f"A {x+36} {y-26} {x+26} {y-14}", f"A {x+42} {y-22} {x+32} {y-10}"]
    if extra == "varactor":
        out += [f"L {x+38} {y-10} {x+38} {y+10}"]
    return out
dl = []
for (x, y, k, lab) in [(10, 30, "plain", "Diode"), (110, 30, "zener", "Zener"), (10, 90, "led", "LED"), (110, 90, "photo", "Photodiode"), (60, 145, "varactor", "Varactor")]:
    dl += diode(x, y, k) + [f"T {x+25} {y+24} 9 {lab}"]
dl.append("T 100 186 7 Forward biased: current flows anode → cathode (bar)")
FIG["diodes"] = (192, "Diode family symbols", j(*dl))

FIG["opamp"] = (130, "Inverting amplifier: Vout = −(Rf / Rin) × Vin", j(
    "G 90 40 90 100 145 70", "t 93 56 10 −", "t 93 90 10 +", "L 54 54 90 54", "Z 22 54 54 54 6", "L 6 54 22 54", "F 6 54 2.5", "L 80 86 90 86", "L 80 86 80 104", "g 80 104",
    "L 145 70 186 70", "F 186 70 2.5", "L 66 54 66 18", "Z 66 18 126 18 7", "L 126 18 160 18", "L 160 18 160 70", "F 160 70 2.5", "F 66 54 2.5",
    "T 38 66 9 Rin", "T 96 8 9 Rf", "T 10 42 9 Vin", "T 178 82 9 Vout"))

FIG["control"] = (110, "Closed-loop control: C/R = G / (1 + GH)", j(
    "A 6 40 36 40", "O 46 40 10", "l 39 33 53 47", "l 39 47 53 33", "A 56 40 82 40", "R 82 24 46 32", "A 128 40 192 40",
    "L 160 40 160 90", "A 160 90 126 90", "R 80 76 46 28", "L 80 90 46 90", "A 46 90 46 52",
    "T 105 44 12 G(s)", "T 103 94 12 H(s)", "t 4 34 8 R(s)", "t 176 34 8 C(s)", "t 60 34 8 E(s)", "t 34 30 8 +", "t 50 62 8 −"))

FIG["stress-strain"] = (140, "Stress-strain curve for a ductile metal", j(
    "L 20 125 196 125", "L 20 125 20 8", "P 20 125 66 40 72 37 80 44 92 41", "B 92 41 120 14 158 14 182 44", "F 182 44 2.5",
    "F 66 40 2.5", "F 74 37 2.5", "F 140 18 2.5", "- 66 40 66 125",
    "t 6 24 8 limit of proportionality →", "t 76 50 8 yield point", "t 122 10 8 UTS", "t 168 58 8 fracture",
    "T 108 136 9 strain", "t 0 8 9 stress", "t 8 100 8 slope = E"))

FIG["sfd-bmd"] = (182, "Simply supported beam, central point load W: SFD and BMD", j(
    "R 20 26 160 8", "G 20 34 12 46 28 46", "G 180 34 172 46 188 46", "A 100 4 100 25", "T 112 10 9 W",
    "L 20 90 180 90", "P 20 90 20 70 100 70 100 110 180 110 180 90", "T 60 66 8 +W/2", "T 140 122 8 −W/2", "t 2 92 8 SFD",
    "L 20 172 180 172", "P 20 172 100 140 180 172", "T 100 133 8 WL/4", "t 2 174 8 BMD", "- 100 140 100 172"))

FIG["four-forces"] = (130, "The four forces in steady level flight: L = W, T = D", j(
    "B 40 65 52 52 100 50 150 56", "L 150 56 176 46", "L 176 46 180 64", "B 180 64 150 72 90 76 40 65",
    "G 92 63 118 63 110 88 98 88", "A 104 60 104 10", "A 104 72 104 124", "A 150 64 196 64", "A 50 66 6 66",
    "T 120 14 10 Lift", "T 124 122 10 Weight", "T 178 92 10 Thrust", "T 22 54 10 Drag"))

FIG["axes"] = (140, "Aircraft axes, all through the centre of gravity", j(
    "B 20 72 40 56 100 54 160 62", "L 160 62 186 48", "L 186 48 190 72", "B 190 72 150 82 80 86 20 72",
    "- 6 70 198 70", "- 104 10 104 134", "- 40 110 168 30", "F 104 70 3",
    "t 4 12 8 YAW · normal axis", "t 116 36 8 PITCH · lateral", "t 112 92 8 ROLL · longitudinal", "T 100 136 7 Ailerons roll, elevator pitches, rudder yaws"))

FIG["blade-element"] = (150, "Blade element: φ = helix angle, α = angle of attack, β = blade angle = φ + α", j(
    "B 66 108 90 86 130 70 166 64", "B 166 64 132 76 96 96 66 108", "- 40 120 182 50",
    "A 40 120 170 120", "A 170 120 170 70", "A 40 120 170 70", "a 40 120 34 -21 21", "a 40 120 52 -27 27",
    "T 105 134 9 Vrot = 2πnr", "t 176 95 9 Vax", "T 84 82 9 Vr", "t 78 114 9 φ", "t 96 104 9 β", "t 150 48 8 chord line"))


# ---------- Electronics: second batch of note diagrams ----------
def gb(x, y, kind):
    """A gate body with its input and output stubs, no label."""
    return [c for c in gate(x, y, kind, "_") if not c.startswith("T ")]

def poly(fn, x0, x1, n=40):
    pts = []
    for i in range(n + 1):
        x = x0 + (x1 - x0) * i / n
        pts += [f(x), f(fn(x))]
    return "P " + " ".join(pts)

ha = gb(90, 20, "xor") + gb(90, 70, "and") + [
    "L 20 27 80 27", "L 20 39 80 39", "L 40 27 40 77", "L 40 77 80 77", "L 56 39 56 89", "L 56 89 80 89",
    "F 40 27 2.5", "F 56 39 2.5", "T 12 27 10 A", "T 12 39 10 B",
    "t 142 33 9 S = A ⊕ B", "t 134 83 9 C = A·B"]
FIG["half-adder"] = (105, "Half adder: S is the sum bit, C the carry. A full adder adds a carry-in too.", j(*ha))

mux = ["G 70 10 130 30 130 100 70 120", "T 100 58 9 4-to-1", "T 100 72 9 MUX", "L 130 65 182 65", "F 182 65 2.5", "T 190 65 10 Y"]
for i, yy in enumerate((30, 52, 74, 96)):
    mux += [f"L 26 {yy} 70 {yy}", f"F 26 {yy} 2.5", f"T 14 {yy} 9 D{i}"]
mux += ["L 90 113 90 130", "L 110 107 110 130", "T 90 140 9 S1", "T 110 140 9 S0"]
FIG["mux"] = (146, "4-to-1 multiplexer: the select lines S1 S0 choose which input reaches Y", j(*mux))

sr = gb(55, 20, "nand") + gb(55, 84, "nand") + gb(120, 20, "nand") + gb(120, 84, "nand") + [
    "L 98 33 104 33", "L 104 33 104 27", "L 104 27 110 27",
    "L 98 97 104 97", "L 104 97 104 103", "L 104 103 110 103",
    "L 163 33 192 33", "L 163 97 192 97",
    "L 170 33 170 56", "L 170 56 107 56", "L 107 56 107 91", "L 107 91 110 91", "F 170 33 2.5",
    "L 177 97 177 70", "L 177 70 101 70", "L 101 70 101 39", "L 101 39 110 39", "F 177 97 2.5",
    "L 14 65 35 65", "L 35 39 35 91", "L 35 39 45 39", "L 35 91 45 91", "F 35 65 2.5",
    "L 14 27 45 27", "L 14 103 45 103", "T 8 27 9 S", "T 8 103 9 R", "T 14 76 8 CLK",
    "T 186 24 9 Q", "T 186 108 9 Q'", "T 72 10 8 1", "T 72 126 8 2", "T 137 10 8 3", "T 137 126 8 4"]
FIG["sr-ff"] = (132, "Clocked SR flip-flop from four NAND gates (gates 3 and 4 are the latch)", j(*sr))

jc = []
for i, x in enumerate((20, 65, 110, 155)):
    jc += [f"R {x} 30 30 40", f"t {x+3} 40 8 D", f"t {x+20} 40 8 Q", f"t {x+18} 60 8 Q'", f"L {x+15} 70 {x+15} 88", f"T {x+15} 100 8 FF{i}"]
    if i < 3:
        jc.append(f"A {x+30} 40 {x+45} 40")
jc += ["L 8 88 170 88", "T 8 80 8 CLK", "L 185 60 193 60", "L 193 60 193 16", "L 193 16 10 16", "L 10 16 10 40", "A 10 40 20 40",
       "T 100 10 8 Q' of the last flip-flop goes back to D of the first", "T 100 114 7 0000→1000→1100→1110→1111→0111→0011→0001"]
FIG["johnson"] = (122, "4-bit Johnson (twisted-ring) counter: 4 flip-flops give 8 states", j(*jc))

dac = ["L 10 15 150 15", "T 14 7 9 Vref"]
for x, d, r in ((30, "D3", "R"), (65, "D2", "2R"), (100, "D1", "4R"), (135, "D0", "8R")):
    dac += [f"L {x} 15 {x} 26", f"F {x} 26 1.8", f"L {x} 26 {x+8} 35", f"F {x} 38 1.8", f"L {x} 38 {x} 46",
            f"Z {x} 46 {x} 76 5", f"L {x} 76 {x} 88", f"t {x+10} 30 8 {d}", f"t {x+9} 62 8 {r}"]
dac += ["L 30 88 150 88", "A 40 82 58 82", "T 49 76 8 I", "G 150 78 150 118 186 98", "t 152 88 9 −", "t 152 109 9 +",
        "L 150 110 144 110", "L 144 110 144 118", "g 144 118",
        "L 118 88 118 138", "L 118 138 150 138", "Z 150 138 180 138 5", "L 180 138 192 138", "L 192 138 192 98", "L 186 98 198 98",
        "F 118 88 2.5", "F 192 98 2.5", "T 165 148 8 Rf", "T 184 74 8 Vout"]
FIG["dac"] = (152, "Binary-weighted DAC: I = (Vref/R)(D3 + D2/2 + D1/4 + D0/8),  Vout = −I·Rf", j(*dac))

sar = ["G 40 28 40 72 72 50", "t 43 40 9 +", "t 43 62 9 −", "A 6 40 40 40", "T 14 32 9 Vin", "L 72 50 92 50",
       "R 92 30 50 40", "T 117 50 10 SAR", "A 117 6 117 30", "T 130 10 8 CLK", "A 142 50 192 50", "T 166 42 8 n-bit out",
       "R 92 86 50 26", "T 117 99 10 DAC", "L 117 70 117 86", "L 92 99 26 99", "L 26 99 26 60", "A 26 60 40 60",
       "T 60 108 8 DAC output"]
FIG["sar-adc"] = (118, "Successive-approximation ADC: each bit is tried from the MSB down and kept if the DAC output ≤ Vin", j(*sar))

mg = ["L 22 8 22 72", "- 22 40 192 40", "P 22 16 70 22 110 40 160 62 192 72",
      "L 22 84 22 152", "- 22 130 192 130", "P 22 90 70 98 110 115 150 130 192 142",
      "- 110 40 110 115", "- 150 58 150 130", "D 150 40 150 58", "D 110 115 110 130",
      "t 26 8 8 |GH| (dB)", "t 170 34 8 0 dB", "t 26 82 8 phase", "t 162 124 8 −180°",
      "t 154 50 9 GM", "t 114 122 9 PM", "T 110 30 8 ωgc", "T 150 142 8 ωpc"]
FIG["margins"] = (154, "Bode plot: gain margin at the phase crossover, phase margin at the gain crossover. Both positive: stable.", j(*mg))

z, wn = 0.3, 1.0
wd = wn * math.sqrt(1 - z * z)
def c_of(t): return 1 - math.exp(-z * wn * t) * (math.cos(wd * t) + z / math.sqrt(1 - z * z) * math.sin(wd * t))
kx = 170 / 16.0
tp = math.pi / wd
st = ["L 20 8 20 116", "L 20 116 196 116", "- 20 52 196 52", "t 4 52 8 1",
      poly(lambda x: 116 - 64 * c_of((x - 20) / kx), 20, 190, 80)]
yp = 116 - 64 * c_of(tp); xp = 20 + tp * kx
ts = 4 / (z * wn); xs = 20 + ts * kx
st += [f"D {f(xp)} 52 {f(xp)} {f(yp)}", f"t {f(xp+4)} {f((yp+52)/2)} 8 Mp", f"- {f(xp)} 52 {f(xp)} 116", f"T {f(xp)} 124 8 tp",
       f"- {f(xs)} 46 {f(xs)} 116", f"T {f(xs)} 124 8 ts", "- 20 46 196 46", "- 20 58 196 58", "t 150 40 7 ±2% band",
       "t 24 16 8 c(t)", "T 186 124 8 t"]
FIG["step-response"] = (130, "Underdamped step response (0 < ζ < 1): overshoot Mp at the peak time tp, settling time ts", j(*st))

ts_ = []
for x0, kind, lab in ((8, "step", "Step: 1/s"), (74, "impulse", "Impulse: 1"), (140, "ramp", "Ramp: 1/s²")):
    ts_ += [f"L {x0} 62 {x0+54} 62", f"L {x0+6} 66 {x0+6} 12", f"T {x0+27} 76 8 {lab}"]
    if kind == "step":
        ts_ += [f"P {x0+6} 62 {x0+16} 62 {x0+16} 30 {x0+52} 30"]
    elif kind == "impulse":
        ts_ += [f"A {x0+16} 62 {x0+16} 16"]
    else:
        ts_ += [f"L {x0+16} 62 {x0+52} 22"]
FIG["test-signals"] = (84, "Standard test signals and their Laplace transforms", j(*ts_))

bd = []
for x0, name, cb, vb, gap in ((14, "Conductor", (16, 46), (36, 70), None), (78, "Semiconductor", (14, 38), (56, 80), "Eg ≈ 1 eV"),
                              (142, "Insulator", (8, 28), (72, 96), "Eg > 5 eV")):
    bd += [f"R {x0} {cb[0]} 46 {cb[1]-cb[0]}", f"H {x0} {vb[0]} {x0+46} {vb[0]} {x0+46} {vb[1]} {x0} {vb[1]}", f"R {x0} {vb[0]} 46 {vb[1]-vb[0]}",
           f"T {x0+23} {(cb[0]+cb[1])/2 if gap else cb[0]+8} 8 CB", f"T {x0+23} {vb[1]-8} 8 VB", f"T {x0+23} 110 8 {name}"]
    if gap:
        bd += [f"D {x0+23} {cb[1]} {x0+23} {vb[0]}", f"t {x0+26} {(cb[1]+vb[0])/2} 7 {gap}"]
bd += ["A 6 100 6 10", "T 6 4 7 E"]
FIG["bands"] = (116, "Energy bands: CB = conduction band, VB = valence band (filled). The forbidden gap Eg decides the class.", j(*bd))

ps = []
names = ("Step-down", "Rectifier", "Filter", "Regulator", "Load")
for i, nm in enumerate(names):
    x = 4 + i * 40
    ps += [f"R {x} 14 32 30", f"T {x+16} 54 7 {nm}"]
    if i < 4:
        ps.append(f"A {x+32} 29 {x+40} 29")
    cx, cy = x + 4, 29
    if i == 0:
        ps.append(poly(lambda xx: cy - 8 * math.sin((xx - cx) / 24 * 4 * math.pi), cx, cx + 24, 32))
    elif i == 1:
        ps.append(poly(lambda xx: cy + 6 - 13 * abs(math.sin((xx - cx) / 24 * 2 * math.pi)), cx, cx + 24, 32))
    elif i == 2:
        ps.append(poly(lambda xx: cy - 5 + 2 * ((xx - cx) % 8) / 8, cx, cx + 24, 48))
    elif i == 3:
        ps.append(f"L {cx} {cy-6} {cx+24} {cy-6}")
    else:
        ps.append(f"Z {cx} {cy} {cx+24} {cy} 5")
ps.append("T 100 70 7 ac in → pulsating dc → smooth dc with ripple → steady dc")
FIG["psu"] = (76, "Regulated dc power supply: the waveform each block hands on", j(*ps))


# ---------- Mech Tech note diagrams ----------
tc = ["E 24 60 8 30", "L 24 30 96 30", "L 24 90 96 90", "E 96 60 8 30",
      "A 106 52 124 52", "A 106 68 124 68", "A 14 52 2 52", "A 14 68 2 68", "T 112 42 9 σL",
      "O 158 60 28", "O 158 60 23", "A 158 60 158 42", "A 158 60 176 60", "A 158 60 158 78", "A 158 60 140 60", "t 162 52 9 p",
      "A 158 22 182 22", "A 158 22 134 22", "t 184 16 9 σH",
      "T 60 108 9 σL = pd / 4t", "T 158 104 9 σH = pd / 2t"]
FIG["thin-cyl"] = (116, "Thin cylinder under internal pressure p: the hoop stress is twice the longitudinal stress", j(*tc))

cx, cy = 48, 62
bal = [f"O {cx} {cy} 3"]
for name, m, r, a in (("A", 100, .3, 0), ("B", 50, .2, 35), ("C", 70, .35, 60), ("D", 90, .15, 120)):
    L = r * 120; x = cx + L * math.cos(math.radians(a)); y = cy - L * math.sin(math.radians(a))
    bal += [f"L {cx} {cy} {f(x)} {f(y)}", f"F {f(x)} {f(y)} {f(2 + m / 30)}", f"t {f(x + 5)} {f(y - 5)} 8 {name}"]
xb = cx + 36 * math.cos(math.radians(221.5)); yb = cy - 36 * math.sin(math.radians(221.5))
bal += [f"- {cx} {cy} {f(xb)} {f(yb)}", f"O {f(xb)} {f(yb)} 5", f"t {f(xb - 12)} {f(yb + 12)} 8 balance", "T 48 112 8 End view"]
px, py, k = 106, 96, 1.55
pts = [(px, py)]
for mr, a in ((30, 0), (10, 35), (24.5, 60), (13.5, 120)):
    x0, y0 = pts[-1]
    pts.append((x0 + k * mr * math.cos(math.radians(a)), y0 - k * mr * math.sin(math.radians(a))))
for (x0, y0), (x1, y1), lab in zip(pts, pts[1:], ("mA·rA", "B", "C", "D")):
    bal += [f"A {f(x0)} {f(y0)} {f(x1)} {f(y1)}"]
bal += [f"A {f(pts[-1][0])} {f(pts[-1][1])} {f(px)} {f(py)}", "t 108 104 8 mA·rA", "T 150 112 8 Force polygon (m·r)",
        "t 116 70 8 Mb·rb"]
FIG["balancing"] = (118, "Balancing in one plane: draw each m·r head to tail; the vector that closes the polygon gives the balance mass", j(*bal))

gt = ["O 24 40 16", "O 50 40 10", "O 82 40 22", "F 24 40 2", "F 50 40 2", "F 82 40 2",
      "T 24 72 8 Driver A", "T 50 62 8 Idler", "T 84 72 8 Driven B", "T 54 88 8 V.R. = TB / TA",
      "O 128 34 12", "O 164 34 24", "O 164 34 10", "O 164 74 20", "F 128 34 2", "F 164 34 2", "F 164 74 2",
      "t 106 18 8 1", "t 186 14 8 2", "t 174 50 8 3", "t 186 90 8 4", "T 150 106 8 V.R. = (T2/T1)(T4/T3)"]
FIG["gear-trains"] = (112, "Simple train (the idler changes the direction, not the ratio) and compound train (gears 2 and 3 share a shaft)", j(*gt))

ep = ["O 64 60 50", "O 64 60 46", "O 64 60 16", "F 64 60 2.5"]
for a in (90, 210, 330):
    x = 64 + 31 * math.cos(math.radians(a)); y = 60 - 31 * math.sin(math.radians(a))
    ep += [f"O {f(x)} {f(y)} 15", f"W 64 60 {f(x)} {f(y)}", f"F {f(x)} {f(y)} 2"]
ep += ["l 108 30 132 22", "t 134 22 8 Annulus", "l 78 34 132 48", "t 134 48 8 Planet", "l 76 66 132 74", "t 134 74 8 Sun",
       "l 52 82 132 100", "t 134 100 8 Arm (carrier)"]
FIG["epicyclic"] = (120, "Simple epicyclic train: planets roll round the sun inside the annulus, carried by the arm", j(*ep))

cl = ["O 46 54 34", "O 46 54 14", "o 46 54 24", "F 46 54 2", "D 46 54 80 54", "D 46 54 46 68", "t 60 62 8 r1", "t 34 76 8 r2", "T 46 104 8 Plate: T = µnWR",
      "L 120 22 160 40", "L 120 86 160 68", "L 160 40 160 68", "L 100 54 198 54", "L 120 22 120 86",
      "- 160 40 191 54", "a 191 54 16 180 24.3", "t 166 48 8 β",
      "A 132 28 140 14", "t 142 14 8 Wn", "A 104 76 118 76", "t 98 70 8 W", "T 150 104 8 Cone: T = µWR cosec β"]
FIG["clutches"] = (112, "Friction clutches: a plate clutch (face view) and a cone clutch (semi-cone angle β)", j(*cl))

bt = ["O 28 44 20", "O 86 44 12", "L 28 24 86 32", "L 28 64 86 56", "F 28 44 2", "F 86 44 2", "T 28 80 7 driver",
      "t 50 20 8 T2 slack", "t 46 72 8 T1 tight", "T 56 92 8 Open",
      "O 132 44 20", "O 182 44 12", "L 132 24 182 56", "L 132 64 182 32", "F 132 44 2", "F 182 44 2", "T 156 92 8 Crossed",
      "T 100 106 7 T1/T2 = e^(µθ),  P = (T1 − T2)v"]
FIG["belts"] = (112, "Open and crossed belt drives (driver on the left turning clockwise)", j(*bt))

def kstr(s):
    return '"' + s.replace("\\", "\\\\").replace('"', '\\"').replace("$", "\\$") + '"'

root = os.path.dirname(os.path.abspath(__file__))
out = os.path.join(root, "..", "app/src/main/java/com/grandpyanne/daemrevise/ui/TopicSketches.kt")
lines = ["package com.grandpyanne.daemrevise.ui", "",
         "// GENERATED by tools/sketches.py: edit the drawings there, then run it again.",
         "// Crafted by Grandpyanne™", "",
         "/** Topic sketches in the drawing language described in tools/sketches.py (200-wide grid). */",
         "internal val SKETCHES: Map<String, String> = mapOf("]
for k in sorted(S):
    lines.append(f"    {kstr(k)} to {kstr(S[k])},")
lines += [")", "", "/** Labelled diagrams for the notes: id to (height on the 200-wide grid, caption, drawing). */",
          "internal val FIGURES: Map<String, Triple<Int, String, String>> = mapOf("]
for k, (h, title, d) in FIG.items():
    lines.append(f"    {kstr(k)} to Triple({h}, {kstr(title)}, {kstr(d)}),")
lines += [")", ""]
open(out, "w", encoding="utf-8").write("\n".join(lines))
if len(sys.argv) > 1:
    json.dump({"S": S, "FIG": FIG}, open(sys.argv[1], "w"), ensure_ascii=False)
print(f"{len(S)} sketches, {len(FIG)} figures -> {out}")
