"""Builds app/src/main/assets/content.json for the study app.

Questions come from the two *_questions.txt files (KNEC past papers).
Summaries, formulas and flashcards are written here per topic.
"""
import json
import re
import sys
from pathlib import Path

from study_content import EXTRAS, LATER
from books import BOOKS, REFS
from maths_content import MATHS_BASICS
from prop_content import PROP_BASICS
from airfield_content import AIRFIELD_BASICS
from airframe_content import AIRFRAME_BASICS
from tof_content import TOF_BASICS
from business_content import BUSINESS_BASICS

HERE = Path(__file__).parent        # tools/
ROOT = HERE.parent                   # project root

# ---------------------------------------------------------------- extras
# topic number -> (summary, [(formula name, formula)], [(card front, card back)])
MECH = {
    1: ("Definitions appear in nearly every paper. Learn each with its SI unit.",
        [("Stress", "σ = P / A  (N/m²)"), ("Strain", "ε = δL / L  (no unit)"),
         ("Young's modulus", "E = σ / ε"), ("Modulus of rigidity", "G = τ / φ"),
         ("Poisson's ratio", "ν = lateral strain / longitudinal strain"),
         ("Bulk modulus", "K = direct stress / volumetric strain"),
         ("Constants linked", "E = 2G(1 + ν) = 3K(1 − 2ν)"),
         ("Resilience (axial)", "U / V = σ² / 2E"), ("Resilience (shear)", "U / V = τ² / 2G"),
         ("Factor of safety", "FoS = ultimate stress / working stress")],
        [("Define Poisson's ratio", "Lateral strain ÷ longitudinal strain. No unit."),
         ("Define modulus of rigidity (G)", "Shear stress ÷ shear strain. Unit N/m²."),
         ("Define bulk modulus (K)", "Direct (hydrostatic) stress ÷ volumetric strain. Unit N/m²."),
         ("State Hooke's law", "Within the elastic limit, stress is directly proportional to strain."),
         ("Define resilience", "Strain energy stored per unit volume. Unit J/m³."),
         ("Define proof resilience", "Maximum strain energy a body can store up to its elastic limit."),
         ("Define modulus of resilience", "Proof resilience per unit volume = σ² / 2E."),
         ("Relationship between E, G and ν", "E = 2G(1 + ν)"),
         ("State Castigliano's first theorem", "The partial derivative of total strain energy with respect to a load gives the deflection at that load in its direction: δ = ∂U/∂W.")]),
    2: ("Find area, then stress, strain and change in length. Lateral change uses Poisson's ratio.",
        [("Extension", "δL = PL / AE"), ("Lateral change", "δd = ν × ε × d"),
         ("Bar in series parts", "δL total = (P/E) Σ (L / A)"),
         ("Strain energy stored", "U = σ² / 2E × A L"),
         ("Design diameter", "Working stress = ultimate / FoS, then A = P / σ")],
        [("Formula for extension of a bar", "δL = PL / AE"),
         ("How do you find change in diameter?", "δd = ν × (σ/E) × d"),
         ("Bar of two sections in series", "Same load in each part; add the extensions: δL = (P/E)(L1/A1 + L2/A2)")]),
    3: ("Both materials shorten by the same amount, and their loads add up to the total.",
        [("Equal strain", "σ1 / E1 = σ2 / E2"), ("Load sharing", "P = σ1 A1 + σ2 A2")],
        [("Two conditions for a compound bar", "1) Equal strain in both: σ1/E1 = σ2/E2.  2) Total load = σ1A1 + σ2A2.")]),
    4: ("Hoop stress is twice longitudinal stress. Use joint efficiency on the matching stress.",
        [("Hoop (circumferential)", "σH = pd / 2t  (÷ ηc with joint efficiency)"),
         ("Longitudinal", "σL = pd / 4t  (÷ ηl with joint efficiency)"),
         ("Hoop strain", "εH = (pd / 4tE)(2 − ν)"),
         ("Longitudinal strain", "εL = (pd / 4tE)(1 − 2ν)"),
         ("Volumetric strain", "δV / V = (pd / 4tE)(5 − 4ν)")],
        [("Hoop stress in a thin cylinder", "σH = pd / 2t"),
         ("Longitudinal stress in a thin cylinder", "σL = pd / 4t, half the hoop stress"),
         ("Volumetric strain of a thin cylinder", "δV/V = (pd/4tE)(5 − 4ν)"),
         ("Four examples of thin cylinders", "Boilers, air receivers, pipes, aircraft fuel and hydraulic tanks")]),
    5: ("The most tested Strength of Materials topic. Learn the torsion equation, its assumptions and the derivation.",
        [("Torsion equation", "T / J = τ / R = Gθ / L"), ("Solid shaft", "J = πd⁴ / 32,  τ = 16T / πd³"),
         ("Hollow shaft", "J = π(D⁴ − d⁴) / 32,  τ = 16TD / π(D⁴ − d⁴)"),
         ("Power", "P = 2πNT / 60  (N in rev/min)"),
         ("Strain energy (solid)", "U = τ² / 4G × volume"),
         ("Strain energy (hollow)", "U = (τ² / 4G)(D² + d²)/D² × volume")],
        [("State the torsion equation", "T/J = τ/R = Gθ/L"),
         ("Three assumptions of simple torsion theory", "Material uniform and obeys Hooke's law; plane sections stay plane and circular; radii stay straight; twist uniform along the length."),
         ("Max shear stress in a solid shaft", "τ = 16T / πd³"),
         ("Polar second moment of a solid shaft", "J = πd⁴ / 32 (m⁴)"),
         ("Torque from power", "T = 60P / 2πN"),
         ("Four factors that torque depends on", "Shaft diameter, allowable shear stress, modulus of rigidity, length and permissible twist.")]),
    6: ("Close-coiled helical springs dominate. Know τ = 8WD/πd³ and the stiffness formula.",
        [("Max shear stress", "τ = 8WD / πd³"), ("Deflection", "δ = 8WD³n / Gd⁴"),
         ("Stiffness", "S = W / δ = Gd⁴ / 8D³n"), ("Angular twist", "θ = 16WD²n / Gd⁴"),
         ("Series springs", "1/K = 1/K1 + 1/K2"), ("Parallel springs", "K = K1 + K2"),
         ("Semi-elliptic leaf spring", "δmax = 3WL³ / 8nEbt³,  σ = 3WL / 2nbt²"),
         ("Energy stored", "U = ½ W δ")],
        [("Max shear stress in a close-coiled spring", "τ = 8WD / πd³"),
         ("Stiffness of a close-coiled spring", "S = Gd⁴ / 8D³n"),
         ("Springs in series", "1/K = 1/K1 + 1/K2"),
         ("Springs in parallel", "K = K1 + K2"),
         ("Close-coiled vs open-coiled", "Close-coiled: helix angle small (under about 10°), coils nearly touching, mainly torsion. Open-coiled: large helix angle, both bending and torsion."),
         ("Two applications of springs", "Landing gear shock absorption, valve return, brakes and clutches, measuring instruments.")]),
    7: ("Expect reactions, shear force and bending moment, plus a deflection or Castigliano question.",
        [("Bending equation", "M / I = σ / y = E / R"),
         ("Section modulus", "Z = I / y_max,  σ = M / Z"),
         ("Cantilever, end load", "δ = WL³ / 3EI"), ("Cantilever, UDL", "δ = wL⁴ / 8EI"),
         ("Simply supported, central load", "δ = WL³ / 48EI,  M max = WL / 4"),
         ("Simply supported, UDL", "δ = 5wL⁴ / 384EI,  M max = wL² / 8"),
         ("Simply supported, eccentric load", "δ under load = Wa²b² / 3EIL"),
         ("Castigliano", "δ = ∂U / ∂W,  U = ∫ M² / 2EI dx")],
        [("State the bending equation", "M/I = σ/y = E/R"),
         ("Four assumptions of simple bending", "Material homogeneous, obeys Hooke's law, same E in tension and compression; plane sections stay plane; beam initially straight; radius of curvature large."),
         ("Define shear force", "Algebraic sum of all vertical forces on one side of a section."),
         ("Define bending moment", "Algebraic sum of moments of all forces on one side of a section."),
         ("Max deflection, cantilever with UDL", "wL⁴ / 8EI"),
         ("Max deflection, simply supported with UDL", "5wL⁴ / 384EI"),
         ("Three factors affecting beam deflection", "Load and its type, span length, flexural rigidity EI (material and section)."),
         ("Define built-in beam", "A beam with both ends fixed so they cannot rotate or move.")]),
    8: ("Parallel axis theorem plus the standard shapes solve every section question.",
        [("Rectangle about centroid", "I = bd³ / 12"), ("Rectangle about base", "I = bd³ / 3"),
         ("Circle about diameter", "I = πd⁴ / 64 = πR⁴ / 4"), ("Polar (circle)", "J = πd⁴ / 32"),
         ("Parallel axis", "I = I_G + A h²"), ("Perpendicular axis", "Izz = Ixx + Iyy"),
         ("Radius of gyration", "k = √(I / A)")],
        [("State the parallel axis theorem", "I about any axis = I about parallel centroidal axis + A h²."),
         ("State the perpendicular axis theorem", "For a plane area, Izz = Ixx + Iyy (z perpendicular to the plane)."),
         ("I of a rectangle about its centroid", "bd³ / 12"),
         ("T-section worked answer (120×10 flange, 20 web, 100 deep)", "Centroid 35 mm below top; I = 3.025 × 10⁶ mm⁴; k = 31.75 mm")]),
    9: ("Use a table of m, r, mr, l, mrl for each plane and close the vector polygon.",
        [("Centrifugal force", "F = m r ω²"), ("Static balance", "Σ m r = 0 (vector sum)"),
         ("Dynamic balance", "Σ m r = 0 and Σ m r l = 0")],
        [("Two conditions for complete dynamic balance", "1) Vector sum of centrifugal forces is zero (Σmr = 0).  2) Vector sum of their couples is zero (Σmrl = 0)."),
         ("Static vs dynamic balancing", "Static: centre of mass on the axis, shaft stays in any position at rest. Dynamic: forces and couples balance when rotating, no bearing vibration.")]),
    10: ("Gear ratio questions and epicyclic trains using the table method appear in most sittings.",
         [("Velocity ratio", "N1 / N2 = T2 / T1"), ("Compound train", "N_in / N_out = product of driven teeth / product of driver teeth"),
          ("Module", "m = d / T"), ("Circular pitch", "p = πm"),
          ("Centre distance", "C = m (T1 + T2) / 2"),
          ("Torque balance", "T_in + T_out + T_fix = 0"),
          ("Efficiency", "η = T_out N_out / T_in N_in"),
          ("Epicyclic (sun, planet, annulus)", "T_annulus = T_sun + 2 T_planet")],
         [("Epicyclic table method, row 1", "Arm fixed, gear A turns +1: B turns −T_A / T_B."),
          ("Epicyclic table method, last step", "Multiply row by x, add y to all; arm speed = y; apply the fixed-gear condition."),
          ("Functions of an idler gear", "Changes the direction of rotation and bridges the gap between shafts; does not change the ratio."),
          ("Why the idler does not change the ratio", "N_A/N_C × N_C/N_B = N_A/N_B, so T_C cancels."),
          ("Define module", "Pitch circle diameter ÷ number of teeth (mm).")]),
    11: ("Know both derivations: uniform pressure and uniform wear. Count pairs of friction surfaces.",
         [("Uniform pressure", "T = n μ W (2/3)(r2³ − r1³)/(r2² − r1²)"),
          ("Uniform wear", "T = n μ W (r1 + r2) / 2"),
          ("Uniform wear pressure", "p r = constant,  W = 2π C (r2 − r1)"),
          ("Cone clutch", "T = μ W R_mean / sin α"),
          ("Power", "P = 2πNT / 60")],
         [("Torque, plate clutch, uniform wear", "T = nμW(r1 + r2)/2"),
          ("Torque, plate clutch, uniform pressure", "T = nμW·(2/3)(r2³ − r1³)/(r2² − r1²)"),
          ("Three types of friction clutch", "Single-plate, multi-plate, cone clutch (also centrifugal)."),
          ("Why design for uniform wear?", "After initial wear the pressure redistributes so wear is uniform; it gives a safer (lower) torque estimate.")]),
    12: ("Tension ratio e^(μθ) and power = (T1 − T2)v cover most belt questions.",
         [("Tension ratio (flat)", "T1 / T2 = e^(μθ),  θ in radians"),
          ("Tension ratio (V-belt)", "T1 / T2 = e^(μθ / sin β)"),
          ("Power", "P = (T1 − T2) v"),
          ("Centrifugal tension", "Tc = m v²"),
          ("Max tension", "T_max = σ × area = T1 + Tc"),
          ("Max power condition", "Tc = T_max / 3,  v = √(T_max / 3m)"),
          ("Open belt angle of lap", "θ = 180° − 2 sin⁻¹((R − r)/C)"),
          ("Crossed belt angle of lap", "θ = 180° + 2 sin⁻¹((R + r)/C)")],
         [("Tension ratio of a flat belt", "T1/T2 = e^(μθ)"),
          ("Power transmitted by a belt", "P = (T1 − T2)v"),
          ("Condition for maximum belt power", "Centrifugal tension = one third of maximum tension."),
          ("Three types of belts", "Flat belt, V-belt, rope (circular) belt."),
          ("Advantages of belts over gears", "Cheaper, quieter, absorb shock, transmit over larger distances.")]),
    13: ("Convert speeds to rad/s first. Many questions add friction torque to the accelerating torque.",
         [("Linear motion", "v = u + at;  s = ut + ½at²;  v² = u² + 2as"),
          ("Angular motion", "ω2 = ω1 + αt;  θ = ω1 t + ½αt²;  ω2² = ω1² + 2αθ"),
          ("Linear-angular link", "v = ωr,  a = αr"), ("Centripetal acceleration", "a = v²/r = ω²r"),
          ("Torque", "T = Iα + friction torque"), ("Kinetic energy", "KE = ½mv² + ½Iω²"),
          ("Impulse", "Ft = mv − mu"), ("Angular momentum", "L = Iω;  I1ω1 = (I1 + I2)ω"),
          ("rev/min to rad/s", "ω = 2πN / 60")],
         [("Define momentum", "Product of mass and velocity, p = mv (kg·m/s)."),
          ("Define impulse", "Force × time for which it acts = change in momentum (N·s)."),
          ("Conservation of linear momentum", "Total momentum of a system stays constant unless an external force acts."),
          ("Newton's second law", "Rate of change of momentum is proportional to the applied force, in its direction: F = ma."),
          ("Flywheel coupled to a stationary rotor", "Conserve angular momentum: N = I1N1/(I1 + I2); KE lost = KE before − KE after.")]),
    14: ("Derivations of rod and disc inertia come up often, along with the parallel axis theorem.",
         [("Mass moment of inertia", "I = m k²"), ("Thin rod about centre", "I = mL² / 12"),
          ("Thin rod about end", "I = mL² / 3"), ("Solid disc about axis", "I = mR² / 2"),
          ("Solid sphere", "I = (2/5) mR²"), ("Parallel axis (mass)", "I = I_G + m h²")],
         [("Define radius of gyration", "Distance from the axis at which the whole mass could be concentrated to give the same moment of inertia: k = √(I/m)."),
          ("I of thin rod about its centre", "mL²/12"), ("I of thin rod about one end", "mL²/3"),
          ("I of a solid disc about its axis", "mR²/2")]),
}

ELEC = {
    1: ("Silicon and germanium have 4 valence electrons. Doping creates N-type and P-type material.",
        [("Silicon atom", "Z = 14, A = 28, neutrons = 14, shells 2, 8, 4"),
         ("Current density", "J = n q μ E = σE")],
        [("Define valence electrons", "Electrons in the outermost shell of an atom; they take part in bonding and conduction."),
         ("Two semiconductor materials", "Silicon and germanium (also gallium arsenide)."),
         ("Silicon atom: protons, neutrons, electrons", "14, 14, 14 (shells 2, 8, 4)."),
         ("N-type material", "Doped with pentavalent impurity (phosphorus, arsenic); majority carriers are electrons."),
         ("P-type material", "Doped with trivalent impurity (boron, gallium); majority carriers are holes."),
         ("Temperature coefficient of semiconductors", "Negative: resistance falls as temperature rises.")]),
    2: ("Learn the current relationships, voltage-divider bias and gain in decibels.",
        [("Currents", "I_E = I_B + I_C"), ("Alpha", "α = I_C / I_E"), ("Beta", "β = I_C / I_B"),
         ("Converting", "β = α/(1 − α);  α = β/(1 + β)"),
         ("Divider bias", "V_B = V_CC R2/(R1 + R2);  V_E = V_B − 0.7;  I_E = V_E / R_E"),
         ("Collector", "V_C = V_CC − I_C R_C;  V_CE = V_C − V_E"),
         ("Gains in dB", "A_v,dB = 20 log A_v;  A_i,dB = 20 log A_i;  A_p,dB = 10 log A_p"),
         ("Negative feedback", "A_f = A / (1 + βA)")],
        [("Convert α = 0.96 to β", "β = 0.96 / 0.04 = 24"),
         ("Biasing of an NPN transistor in the active region", "Emitter-base forward biased, collector-base reverse biased."),
         ("Transistor as a switch", "Cut-off = OFF (no base current); saturation = ON (V_CE ≈ 0.2 V)."),
         ("Merit of voltage-divider bias", "Stable operating point, nearly independent of β and temperature."),
         ("Purpose of coupling capacitors", "Pass the AC signal between stages while blocking DC."),
         ("Purpose of emitter bypass capacitor", "Removes AC negative feedback across R_E, raising the voltage gain.")]),
    3: ("Rectifier formulas and the special diodes in your notes are the likely test questions.",
        [("Peak voltage", "Vm = √2 × V_rms"),
         ("Half-wave", "V_dc = Vm/π;  PIV = Vm;  ripple factor 1.21;  η max 40.6%"),
         ("Full-wave centre-tap", "V_dc = 2Vm/π;  PIV = 2Vm;  ripple 0.48;  η max 81.2%"),
         ("Bridge", "V_dc = 2Vm/π;  PIV = Vm;  4 diodes"),
         ("Silicon diode drop", "Peak across load = Vm − 0.7 V"),
         ("Load current", "I_dc = V_dc / R_L")],
        [("Blocks of a regulated power supply", "Transformer → rectifier → filter → regulator."),
         ("PIV of a half-wave rectifier", "Vm"), ("PIV of a centre-tap full-wave rectifier", "2Vm"),
         ("Zener diode use", "Voltage regulation; operates in reverse breakdown."),
         ("Varactor diode use", "Voltage-variable capacitor for tuning circuits; operated reverse biased."),
         ("Photodiode principle", "Reverse biased; reverse current rises with light intensity."),
         ("SCR structure", "Four layers PNPN, three junctions, terminals anode, cathode, gate."),
         ("Advantages of controlled rectifiers", "Variable output voltage; switch large power with a small gate signal.")]),
    4: ("Op-amp definitions, CMRR and the summer circuit are quick marks.",
        [("CMRR", "CMRR = A_d / A_cm;  in dB = 20 log(A_d / A_cm)"),
         ("Inverting amplifier", "Vo = −(Rf / Rin) Vin"),
         ("Non-inverting amplifier", "Vo = (1 + Rf / R1) Vin"),
         ("Summer", "Vo = −Rf (V1/R1 + V2/R2 + V3/R3)"),
         ("Colpitts frequency", "f = 1 / 2π√(L C_T),  C_T = C1C2 / (C1 + C2)")],
        [("Define slew rate", "Maximum rate of change of op-amp output voltage (V/µs)."),
         ("Define CMRR", "Ratio of differential gain to common-mode gain; ability to reject signals common to both inputs."),
         ("Define drift", "Change in output (offset) with temperature or time when input is constant."),
         ("Define a clamper", "A circuit that shifts a waveform's DC level without changing its shape. Used in TV receivers and to restore DC levels."),
         ("CMRR worked: Ad = 100, Acm = 0.5", "CMRR = 200 = 46 dB")]),
    5: ("Guaranteed marks every paper. Practise each conversion method once tonight.",
        [("Decimal → base b", "Integer: divide by b, read remainders upward. Fraction: multiply by b, read integer parts downward."),
         ("Binary ↔ octal / hex", "Group bits in 3s (octal) or 4s (hex) from the point."),
         ("BCD", "Each decimal digit as 4 bits"), ("Excess-3", "Add 3 to each digit, then 4 bits"),
         ("ASCII digit", "30H + digit"),
         ("Binary → Gray", "Keep MSB, then XOR neighbouring bits"),
         ("Gray → binary", "Keep MSB, then XOR each Gray bit with the previous binary bit")],
        [("(1101011)₂ to octal", "(153)₈"), ("(73.75)₁₀ to binary", "(1001001.11)₂"),
         ("(73.75)₁₀ to octal", "(111.6)₈"), ("(384.625)₁₀ to hex", "(180.A)₁₆"),
         ("(3.CDE)₁₆ to base 4", "(3.303132)₄"), ("(F2CD)₁₆ − (EF6A)₁₆", "(363)₁₆"),
         ("3789 to Excess-3", "0110 1010 1011 1100"), ("3789 to ASCII", "33H 37H 38H 39H"),
         ("(101100)₂ to Gray", "111010"), ("Gray 11100111 to binary", "10111010")]),
    6: ("De Morgan's theorems and simplification rules appear in almost every paper.",
        [("De Morgan 1", "(A + B)' = A'·B'"), ("De Morgan 2", "(A·B)' = A' + B'"),
         ("Absorption", "A + AB = A"), ("Redundancy", "A + A'B = A + B"),
         ("Distribution", "(A + B)(A + C) = A + BC"), ("Combining", "AB + AB' = A")],
        [("A + 1 = ?", "1"), ("A · 1 = ?", "A"), ("A + A + A = ?", "A"), ("A + AB = ?", "A"),
         ("(A + B)(A + C) = ?", "A + BC"), ("State De Morgan's theorems", "(A + B)' = A'B' and (AB)' = A' + B'"),
         ("Define fan-in", "Number of inputs a gate can accept."),
         ("Define fan-out", "Number of gate inputs one output can drive reliably."),
         ("Positive vs negative logic", "Positive: high voltage = 1. Negative: low voltage = 1."),
         ("Five logic families", "RTL, DTL, TTL, ECL, CMOS."),
         ("Engine-start logic (fuel, doors, fault check)", "Y = ABC, one 3-input AND gate.")]),
    7: ("Definitions plus one design (multiplexer or code converter) each sitting.",
        [("4-to-1 MUX output", "f = S1'S0'D0 + S1'S0D1 + S1S0'D2 + S1S0D3"),
         ("Binary → Gray (4-bit)", "G3 = B3, G2 = B3⊕B2, G1 = B2⊕B1, G0 = B1⊕B0")],
        [("Define multiplexer", "Selects one of many inputs to a single output using select lines."),
         ("Define demultiplexer", "Routes one input to one of many outputs."),
         ("Define encoder", "Converts 2ⁿ input lines into an n-bit code."),
         ("Define decoder", "Converts an n-bit code into one of 2ⁿ output lines.")]),
    8: ("Truth tables for SR, JK, D, T and memory chip arithmetic.",
        [("SR", "00 hold, 01 reset, 10 set, 11 invalid"), ("JK", "00 hold, 01 reset, 10 set, 11 toggle"),
         ("D", "Q next = D"), ("T", "0 hold, 1 toggle"),
         ("Chips needed", "(total words × bits) / (chip words × bits)"),
         ("Address lines", "2ⁿ = number of words")],
        [("T flip-flop from JK", "Connect J and K together as the T input."),
         ("JK with J = K = 1", "Output toggles on each clock pulse."),
         ("SR NAND latch invalid input", "S = R = 0 (active-low inputs)."),
         ("64K×8 from 16K×4 chips", "8 chips; 2-to-4 decoder; 14 address lines per chip."),
         ("Johnson counter", "Shift register with inverted output of last stage fed back to first; n flip-flops give 2n states.")]),
    9: ("Short theory: ADC terms and an LCD description.",
        [("ADC resolution", "Resolution = V_ref / 2ⁿ")],
        [("Define ADC resolution", "Smallest change in analog input that changes the digital output by one bit."),
         ("Define ADC accuracy", "How closely the digital output matches the true analog value."),
         ("Successive approximation ADC", "SAR sets bits MSB first, DAC output compared with input each step; n bits take n clock cycles.")]),
    10: ("Block reduction or Mason's formula is worth 7 to 8 marks every sitting.",
         [("Series", "G = G1 G2"), ("Parallel", "G = G1 ± G2"),
          ("Negative feedback", "C/R = G / (1 + GH)"), ("Positive feedback", "C/R = G / (1 − GH)"),
          ("Mason's gain formula", "T = Σ Pk Δk / Δ"),
          ("Δ", "1 − Σ loops + Σ non-touching pairs − …")],
         [("Define plant", "The system or process being controlled."),
          ("Define control element", "Produces the control signal to the plant from the error signal."),
          ("Three merits of open-loop systems", "Simple, cheap, stable, easy to maintain."),
          ("Merits of closed-loop systems", "Accurate; less sensitive to disturbances and parameter changes."),
          ("Define transfer function", "Laplace transform of output ÷ Laplace transform of input, zero initial conditions."),
          ("Define a node (signal flow graph)", "A point representing a system variable."),
          ("Define a source node", "A node with only outgoing branches."),
          ("Define non-touching loops", "Loops that share no common node.")]),
    11: ("Write KVL/KCL in the s-domain, then take output over input.",
         [("Impedances (s-domain)", "R → R,  L → sL,  C → 1/sC"),
          ("Phase-lead network", "Vo/Vi = α(1 + sT)/(1 + αsT),  α < 1")],
         [("Three effects of compensating networks", "Improve stability margins, speed of response, and steady-state accuracy."),
          ("Analog computer: integrator symbol", "Triangle amplifier with capacitor feedback (output = −∫ input)."),
          ("Analog computer: coefficient potentiometer", "Circle symbol multiplying a signal by a constant less than 1.")]),
    12: ("Routh arrays for a range of K are the most common stability question.",
         [("Routh-Hurwitz", "Stable if no sign change in the first column"),
          ("Gain margin", "GM = −20 log|G(jω)| at the phase crossover frequency (phase = −180°)"),
          ("Phase margin", "PM = 180° + ∠G(jω) at the gain crossover frequency (|G| = 1, 0 dB)")],
         [("State the Routh-Hurwitz criterion", "A system is stable if all elements of the first column of the Routh array have the same sign."),
          ("Define phase crossover frequency", "Frequency at which the phase of G(jω)H(jω) is −180°."),
          ("Define gain crossover frequency", "Frequency at which |G(jω)H(jω)| = 1 (0 dB)."),
          ("Demerits of Routh-Hurwitz", "Gives only absolute stability, not relative stability; valid only for polynomial characteristic equations."),
          ("Advantages of Bode plots", "Easy asymptotic sketching; margins read directly; multiplication becomes addition; works from experimental data.")]),
    13: ("Know the standard test signals and how to get the characteristic equation.",
         [("Closed loop (unity feedback)", "C/R = G / (1 + G)"),
          ("Characteristic equation", "1 + G(s)H(s) = 0")],
         [("Unit step", "r(t) = 1 for t ≥ 0, 0 for t < 0; R(s) = 1/s."),
          ("Unit ramp", "r(t) = t for t ≥ 0; R(s) = 1/s².")]),
}

# Units not built yet appear as 'Coming soon': (id, name, note).
OTHER_UNITS = []


# Who studies which units. Units are written once (above) and listed by id here,
# so a unit shared by two courses or options is never duplicated.
# To add a course later (Avionics, Flight Dispatch, Cabin Crew…), add an entry here.
CATALOG = [
    {
        "id": "daem",
        "name": "Diploma in Aeronautical Engineering",
        "short": "DAEM",
        "options": [
            {
                "id": "ae",
                "name": "Airframes & Engines",
                "years": [
                    {"id": "y2", "name": "Year 2",
                     "unitIds": ["mechtech", "electronics", "maths2", "propellers",
                                 "airfield", "airframe", "tof", "business"]},
                ],
            },
        ],
    },
]


def parse(path: Path, prefix: str, extras: dict):
    topics, cur = [], None
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line:
            continue
        if line.startswith("## "):
            n, title, section = [p.strip() for p in line[3:].split("|")]
            n = int(n)
            summary, formulas, cards = extras.get(n, ("", [], []))
            cur = {
                "id": f"{prefix}-{n}",
                "title": title,
                "section": section,
                "summary": summary,
                "formulas": [{"name": a, "expr": b} for a, b in formulas],
                "cards": [{"front": a, "back": b} for a, b in cards],
                "questions": [],
            }
            tid = cur["id"]
            extra = EXTRAS.get(tid, {})
            cur["status"] = extra.get("status", "later" if tid in LATER else "covered")
            cur["notes"] = extra.get("notes", [])
            cur["pdfs"] = extra.get("pdfs", [])
            cur["refs"] = REFS.get(tid, [])
            cur["mcqs"] = [
                {"q": m["q"], "options": m["o"], "answer": 0, "explain": m["e"]}
                for m in extra.get("mcqs", [])
            ]
            topics.append(cur)
            continue
        q, _, src = line.partition(";;")
        cur["questions"].append({"q": q.strip(), "src": src.strip()})
    missing = [n for n in extras if f"{prefix}-{n}" not in {t["id"] for t in topics}]
    if missing:
        sys.exit(f"extras with no topic in {path.name}: {missing}")
    return topics


# Units that are really two subjects: the app shows one swipeable tab per part.
# section name (from *_questions.txt) -> part name. Other units have no parts.
PARTS = {
    "mechtech": {"Strength of Materials": "Strength of Materials", "Mechanics of Machines": "Mechanics of Machines"},
    "electronics": {"Analog Electronics": "Analog", "Digital Electronics": "Digital", "Control Systems": "Control Systems"},
    "airfield": {"Airfield safety": "Airfield Safety II", "Research methods": "Research Methods"},
}


def main():
    out = ROOT / "app/src/main/assets/content.json"
    out.parent.mkdir(parents=True, exist_ok=True)
    units = [
        {"id": "mechtech", "name": "Aircraft Mechanical Technology I", "short": "Mech Tech I",
         "note": "KNEC 2506/205 · 13 sittings, 2017 – April 2025", "ready": True,
         "topics": parse(HERE / "mechtech_questions.txt", "mt", MECH)},
        {"id": "electronics", "name": "Electronics & Control Systems", "short": "Electronics",
         "note": "KNEC 2506/202 · questions from 2016, 2017, 2024, 2025 papers and class notes", "ready": True,
         "topics": parse(HERE / "electronics_questions.txt", "ec", ELEC)},
        {"id": "maths2", "name": "Engineering Mathematics II", "short": "Eng Maths II",
         "note": "KNEC Module II · 8 sittings 2017 – 2024, the 2025 Electrical paper and a mock", "ready": True,
         "topics": parse(HERE / "maths_questions.txt", "mm", MATHS_BASICS)},
        {"id": "propellers", "name": "Aircraft Propeller Systems", "short": "Propellers",
         "note": "KNEC Module II · sittings 2022 – April 2025", "ready": True,
         "topics": parse(HERE / "prop_questions.txt", "pp", PROP_BASICS)},
        {"id": "airfield", "name": "Airfield Safety II & Research Methods", "short": "Airfield & Research",
         "note": "KNEC Module II · sittings 2021 – April 2025", "ready": True,
         "topics": parse(HERE / "airfield_questions.txt", "ar", AIRFIELD_BASICS)},
        {"id": "airframe", "name": "Airframe Systems I", "short": "Airframe Systems I",
         "note": "KNEC Module II · sittings 2022 – Nov 2024", "ready": True,
         "topics": parse(HERE / "airframe_questions.txt", "as", AIRFRAME_BASICS)},
        {"id": "tof", "name": "Theory of Flight", "short": "Theory of Flight",
         "note": "KNEC Module II · sittings 2019 – Nov 2023", "ready": True,
         "topics": parse(HERE / "tof_questions.txt", "tf", TOF_BASICS)},
        {"id": "business", "name": "Business Plan", "short": "Business Plan",
         "note": "Class notes, template and pop quiz", "ready": True,
         "topics": parse(HERE / "business_questions.txt", "bp", BUSINESS_BASICS)},
    ]
    for uid, name, note in OTHER_UNITS:
        units.append({"id": uid, "name": name, "short": name, "note": note, "ready": False, "topics": []})
    for u in units:
        mapping = PARTS.get(u["id"], {})
        for t in u["topics"]:
            if mapping and t["section"] not in mapping:
                sys.exit(f"{t['id']}: section {t['section']!r} has no part in PARTS")
            t["part"] = mapping.get(t["section"], "")
        u["parts"] = list(dict.fromkeys(mapping.values()))
    ids = {t["id"] for u in units for t in u["topics"]}
    unknown = set(EXTRAS) - ids
    if unknown:
        sys.exit(f"EXTRAS for unknown topics: {unknown}")
    for tid, ex in EXTRAS.items():
        for m in ex.get("mcqs", []):
            if len(m["o"]) != 4 or len(set(m["o"])) != 4:
                sys.exit(f"{tid}: MCQ needs 4 distinct options: {m['q']}")
    unknown = set(REFS) - ids
    if unknown:
        sys.exit(f"REFS for unknown topics: {unknown}")
    book_ids = {b["id"] for b in BOOKS}
    for tid, refs in REFS.items():
        for r in refs:
            if r["book"] not in book_ids:
                sys.exit(f"{tid}: unknown book {r['book']}")
    # Which units each book serves, so the Textbooks screen can group them by unit.
    for b in BOOKS:
        b["units"] = [u["short"] for u in units
                      if any(r["book"] == b["id"] for t in u["topics"] for r in t["refs"])]
        # Where the book opens from each unit's shelf: that unit's earliest linked page.
        b["opens"] = {}
        for u in units:
            pages = sorted((r["page"], r["where"]) for t in u["topics"] for r in t["refs"]
                           if r["book"] == b["id"] and r["page"] > 0)
            if pages:
                b["opens"][u["short"]] = {"page": pages[0][0], "where": pages[0][1]}
    data = {"version": 4, "updated": "2026-10-29", "units": units, "courses": CATALOG, "books": BOOKS}
    listed = {uid for c in CATALOG for o in c["options"] for y in o["years"] for uid in y["unitIds"]}
    missing = listed - {u["id"] for u in units}
    if missing:
        sys.exit(f"CATALOG lists units that do not exist: {missing}")
    out.write_text(json.dumps(data, ensure_ascii=False, indent=1), encoding="utf-8")
    for u in units:
        nq = sum(len(t["questions"]) for t in u["topics"])
        nc = sum(len(t["cards"]) for t in u["topics"])
        nm = sum(len(t["mcqs"]) for t in u["topics"])
        nn = sum(1 for t in u["topics"] if t["notes"])
        nr = sum(len(t["refs"]) for t in u["topics"])
        print(f"{u['short']:<40} topics={len(u['topics']):>2} questions={nq:>3} cards={nc:>3} mcqs={nm:>3} notes={nn:>2} refs={nr:>2}")
    print("wrote", out, out.stat().st_size, "bytes")


if __name__ == "__main__":
    main()
