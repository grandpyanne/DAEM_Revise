"""Textbooks bundled inside the app, and where each topic sits in them.

BOOKS: one entry per book. `file` is the PDF in app/src/main/assets/books/
(compressed copies of the PDFs in your DAEM folders, same pages).

REFS: topic id -> list of (book id, where to read, PDF page or 0).
The PDF page is the page the in-app reader opens at (not the printed page number).
All page numbers were checked against the PDFs on Erick's PC.
"""

BOOKS = [
    dict(id="bansal", title="A Textbook of Strength of Materials", author="R.K. Bansal", file="books/bansal.pdf"),
    dict(id="khurmi", title="Theory of Machines", author="R.S. Khurmi & J.K. Gupta", file="books/khurmi.pdf"),
    dict(id="bird", title="Mechanical Engineering Principles", author="John Bird & Carl Ross", file="books/bird.pdf"),
    dict(id="dass", title="Higher Engineering Mathematics", author="H.K. Dass", file="books/dass.pdf"),
    dict(id="sc17", title="Propeller (B1) training notes", author="British Airways Engineering Training", file="books/sc17.pdf"),
    dict(id="easa17", title="EASA Part-66 Module 17: Propellers", author="Module 17 study book", file="books/easa17.pdf"),
    dict(id="kermode", title="Mechanics of Flight (11th ed.)", author="A.C. Kermode", file="books/kermode.pdf"),
    # FAA handbooks (US government works, free to share). Trimmed to the chapters in the outlines;
    # pages below are already for the trimmed copies.
    dict(id="faa_airframe", title="Aviation Maintenance Technician Handbook – Airframe (systems chapters)",
         author="FAA-H-8083-31 · ch 12, 13, 16 and 9", file="books/faa_airframe.pdf"),
    dict(id="faa_heli", title="Helicopter Flying Handbook", author="FAA-H-8083-21A · ch 1–4 and 11", file="books/faa_heli.pdf"),
    dict(id="faa_general", title="Aviation Maintenance Technician Handbook – General",
         author="FAA-H-8083-30A · ch 1, 2, 6, 10 and logic gates", file="books/faa_general.pdf"),
]

R = lambda book, where, page=0: dict(book=book, where=where, page=page)

REFS = {
    # ---- Mech Tech (Bansal +17, Khurmi +10, Bird +11) ----
    "mt-1": [R("bansal", "Ch 1 Simple stresses & strains — 1.2 to 1.9", 18),
             R("bansal", "Ch 2 Elastic constants (E, G, K, Poisson's ratio)", 76),
             R("bird", "Ch 3 The effects of forces on materials", 58),
             R("bird", "Ch 4 Tensile testing", 75)],
    "mt-2": [R("bansal", "1.10 Bars of varying sections", 31),
             R("bansal", "1.16 Elongation due to own weight", 67)],
    "mt-3": [R("bansal", "1.13 Bars of composite sections", 47),
             R("bansal", "1.14–1.15 Thermal stresses & composite bars", 59)],
    "mt-4": [R("bansal", "Ch 17 Thin cylinders and spheres", 764)],
    "mt-5": [R("bansal", "Ch 16 Torsion of shafts — 16.1 to 16.8", 696),
             R("bansal", "16.11 Composite shafts", 730),
             R("bird", "Ch 12 Twisting of shafts", 173)],
    "mt-6": [R("bansal", "16.14 Springs (close-coiled helical, leaf)", 745),
             R("khurmi", "Ch 4 SHM — 4.7 Closely-coiled helical spring", 88)],
    "mt-7": [R("bansal", "Ch 6 Shear force & bending moment", 254),
             R("bansal", "Ch 7 Bending stresses in beams", 312),
             R("bansal", "Ch 12 Deflection of beams (12.7 Macaulay)", 532),
             R("bird", "Ch 10 Bending of beams", 157)],
    "mt-8": [R("bansal", "Ch 5 Centre of gravity & moment of inertia", 188),
             R("bansal", "5.8–5.9 Perpendicular & parallel axis theorems", 213),
             R("bird", "Ch 9 First and second moments of area", 139)],
    "mt-9": [R("khurmi", "Ch 21 Balancing of rotating masses", 843)],
    "mt-10": [R("khurmi", "Ch 13 Gear trains (13.3 simple, 13.4 compound, 13.7 epicyclic)", 438)],
    "mt-11": [R("khurmi", "10.31–10.34 Friction clutches (plate, multi-plate, cone)", 307)],
    "mt-12": [R("khurmi", "Ch 11 Belt drives (11.11 belt length, 11.13 power)", 335)],
    "mt-13": [R("khurmi", "Ch 2 Kinematics of motion", 18),
              R("khurmi", "Ch 3 Kinetics — 3.17 impulse, 3.18 momentum, 3.21 collisions", 49),
              R("khurmi", "16.8–16.10 Flywheel & fluctuation of speed", 580),
              R("bird", "Ch 15 Linear and angular motion", 212),
              R("bird", "Ch 16 Linear momentum and impulse", 221)],
    "mt-14": [R("khurmi", "3.10 Mass moment of inertia, 3.11 angular momentum", 38),
              R("khurmi", "3.20 Torque to accelerate a geared system", 58)],
    # ---- Electronics & control ----
    # ---- Maths (Dass +16) ----
    "mm-1": [R("dass", "Ch 4 Determinants & matrices", 239),
             R("dass", "4.30 Inverse of a matrix", 300),
             R("dass", "4.44 Cramer's rule", 327),
             R("dass", "4.51 Eigen values, 4.52 Cayley–Hamilton", 341)],
    "mm-2": [R("dass", "1.26 Taylor's series of two variables", 83),
             R("dass", "Ch 20 Infinite series", 1135)],
    "mm-3": [R("dass", "Ch 1 Partial differentiation", 17)],
    "mm-4": [R("dass", "Ch 3 Differential equations — 3.5 first order", 156),
             R("dass", "3.7 Homogeneous equations", 158),
             R("dass", "3.9 Linear equations (integrating factor)", 163),
             R("dass", "3.11 Exact equations", 170)],
    "mm-5": [R("dass", "3.18 Second order with constant coefficients", 190),
             R("dass", "3.20 Complementary function", 191)],
    "mm-6": [R("dass", "Ch 13 Laplace transformation", 901),
             R("dass", "13.20 Inverse Laplace transforms", 922),
             R("dass", "13.30 Solving differential equations", 933)],
    "mm-7": [R("dass", "Ch 5 Vectors (5.16 scalar triple product)", 388),
             R("dass", "5.26 Gradient", 402),
             R("dass", "5.29 Divergence & curl", 414)],
    "mm-8": [R("dass", "Ch 10 Statistics (10.16 correlation, 10.22 regression)", 751),
             R("dass", "Ch 11 Probability", 779),
             R("dass", "Binomial distribution", 797),
             R("dass", "Poisson distribution", 810),
             R("dass", "Normal distribution", 822)],
    # ---- Propellers (SC17 page = PDF page) ----
    "pp-1": [R("sc17", "Thrust & torque, terminology, blade twist, efficiency", 3),
             R("sc17", "Centrifugal & aerodynamic turning moments", 20),
             R("easa17", "17.1 Fundamentals", 3),
             R("easa17", "Study questions: 17.1 & 17.2", 22),
             R("faa_powerplant", "Ch 7 Propellers — basic propeller principles")],
    "pp-2": [R("sc17", "Propeller control, control units, PCU operation", 24),
             R("sc17", "Feathering system, fine-pitch stop, pitch locks", 32),
             R("easa17", "17.3 Propeller pitch control", 24),
             R("faa_powerplant", "Ch 7 Propellers — governors & feathering")],
    "pp-3": [R("sc17", "Synchronising", 38),
             R("sc17", "Balancing", 42),
             R("sc17", "Fluid & electrical de-icing", 52),
             R("easa17", "17.4 Synchronising", 40),
             R("easa17", "17.5 Ice protection", 45)],
    "pp-4": [R("sc17", "Maintenance, repairs, storage, installation", 41),
             R("sc17", "Composite blades", 58),
             R("easa17", "17.2 Propeller construction", 10),
             R("easa17", "17.6 Propeller maintenance", 50),
             R("faa_powerplant", "Ch 7 Propellers — inspection & maintenance")],
    "pp-5": [R("easa17", "17.6 Propeller maintenance", 50),
             R("easa17", "Study questions (17.3 onwards)", 70)],
    # ---- Airfield ----
    "ar-1": [R("faa_wb", "Ch 1–2 Weight & balance control, procedures"),
             R("faa_phak", "Ch 10 Weight and balance")],
    "ar-3": [R("faa_phak", "Ch 14 Airport operations — markings, signs, lighting, light-gun signals")],
    "ar-5": [R("kothari", "Ch 1 Research methodology: an introduction; Ch 2 Defining the research problem")],
    "ar-6": [R("kothari", "Ch 4 Sampling design; Ch 6 Methods of data collection")],
    "ar-7": [R("kothari", "Ch 7 Processing & analysis of data; Ch 14 Interpretation & report writing")],
    # ---- Airframe ----
    "as-1": [R("faa_airframe", "Ch 12 Hydraulic and pneumatic power systems")],
    "as-2": [R("faa_airframe", "Ch 12 Hydraulic and pneumatic power systems — pneumatic systems")],
    "as-3": [R("faa_airframe", "Ch 13 Aircraft landing gear systems")],
    "as-4": [R("faa_airframe", "Ch 13 Landing gear — wheels, tyres, brakes, anti-skid")],
    "as-5": [R("faa_airframe", "Ch 16 Cabin environmental control systems")],
    "as-6": [R("faa_airframe", "Ch 9 Aircraft electrical system")],
    # ---- Theory of flight (Kermode +9) ----
    "tf-1": [R("kermode", "Ch 9 Stability and control", 266),
             R("faa_phak", "Ch 5 Aerodynamics of flight — stability")],
    "tf-2": [R("kermode", "Ch 3 Aerofoils (stall, flaps)", 76),
             R("kermode", "Ch 6 Gliding and landing (flaps & slots)", 189),
             R("kermode", "Ch 11 Transonic flight (swept wings)", 306)],
    "tf-3": [R("kermode", "Ch 5 Level flight", 156),
             R("kermode", "Ch 7 Performance", 224),
             R("faa_phak", "Ch 11 Aircraft performance")],
    "tf-4": [R("kermode", "Ch 8 Manoeuvres (turns, load factor)", 239),
             R("faa_phak", "Ch 5 Aerodynamics of flight — turns & load factors")],
    "tf-5": [R("faa_heli", "Ch 2 Aerodynamics of flight")],
}


# Only keep references to books that are bundled in the app.
_BUNDLED = {b["id"] for b in BOOKS}
REFS = {tid: kept for tid, refs in REFS.items() if (kept := [r for r in refs if r["book"] in _BUNDLED])}


# ---- Trimming -----------------------------------------------------------------
# PDF page ranges kept in the app's copy of each book, chosen from Erick's course
# outlines (Mech Tech I, Eng Maths II, Theory of Flight). Books not listed are whole.
# Page numbers in REFS above are for the FULL book; they are renumbered below.
KEEP = {
    # Partial diff 1, ODEs 3, matrices 4, vectors 5, statistics 10, probability 11,
    # Laplace 13, infinite/power series 20 (+ contents at the front)
    "dass": [(1, 106), (154, 482), (751, 863), (901, 948), (1135, 1173)],
    # 1-2 stress/strain & elastic constants, 4-7 strain energy, MOI, SF&BM, bending,
    # 12-17 deflection, cantilevers, propped/fixed beams, torsion & springs, thin cylinders,
    # 26 objective questions
    "bansal": [(1, 101), (160, 361), (532, 805), (1108, 1162)],
    # 1-4 kinematics, kinetics, SHM; 10-13 friction & clutches, belts, gearing, gear trains;
    # 16 flywheel; 21 balancing; 26 automatic control
    "khurmi": [(1, 103), (268, 489), (575, 621), (843, 867), (1060, 1072)],
    # drops 23-28 (heat, thermal expansion, hydrostatics, fluid flow, gases, temperature)
    "bird": [(1, 293), (371, 389)],
    # drops 12 supersonic flight and 13 space flight
    "kermode": [(1, 350), (421, 513)],
}


def new_page(book, page):
    """Page number in the trimmed book for a page of the full book (0 stays 0)."""
    ranges = KEEP.get(book)
    if not ranges or page == 0:
        return page
    before = 0
    for lo, hi in ranges:
        if lo <= page <= hi:
            return before + (page - lo + 1)
        before += hi - lo + 1
    raise ValueError(f"{book} page {page} is in a trimmed-out part")


REFS = {tid: [dict(r, page=new_page(r["book"], r["page"])) for r in refs] for tid, refs in REFS.items()}


# ---- FAA handbooks (page numbers are for the trimmed copies in assets/books) ----------
FAA_REFS = {
    "as-1": [R("faa_airframe", "Ch 12 Hydraulic and pneumatic power systems", 1),
             R("faa_airframe", "Hydraulic fluids", 2)],
    "as-2": [R("faa_airframe", "Pneumatic systems", 48)],
    "as-3": [R("faa_airframe", "Ch 13 Landing gear types and systems", 55),
             R("faa_airframe", "Shock absorbing struts", 59),
             R("faa_airframe", "Nose wheel steering", 84)],
    "as-4": [R("faa_airframe", "Aircraft wheels", 88),
             R("faa_airframe", "Aircraft brakes", 89),
             R("faa_airframe", "Anti-skid systems", 111),
             R("faa_airframe", "Aircraft tyres", 130)],
    "as-5": [R("faa_airframe", "Ch 16 Cabin environmental control systems", 151),
             R("faa_airframe", "Oxygen systems", 154),
             R("faa_airframe", "Pressurisation systems", 171),
             R("faa_airframe", "Air conditioning systems", 184)],
    "as-6": [R("faa_airframe", "Ch 9 Aircraft electrical system", 213)],
    "tf-5": [R("faa_heli", "Ch 1 Introduction to the helicopter (construction)", 15),
             R("faa_heli", "Ch 2 Aerodynamics of flight", 23),
             R("faa_heli", "Autorotation", 46),
             R("faa_heli", "Ch 3 Flight controls: collective, cyclic, anti-torque", 51),
             R("faa_heli", "Main rotor systems: rigid, semirigid, fully articulated", 58),
             R("faa_heli", "Swash plate and freewheeling unit", 62),
             R("faa_heli", "Transmission system", 66),
             R("faa_heli", "Autorotation as an emergency procedure", 76)],
    "ar-1": [R("faa_general", "Ch 6 Aircraft weight and balance", 79)],
    "ar-3": [R("faa_general", "Ch 1 Safety, ground operations and servicing", 1),
             R("faa_general", "Taxiing and hand signals", 21)],
    "ar-4": [R("faa_general", "Ch 2 Regulations, maintenance forms, records and publications", 31),
             R("faa_general", "Airworthiness directives", 36),
             R("faa_general", "Maintenance records", 48),
             R("faa_general", "Ch 10 Inspection concepts and techniques", 113)],
    "ec-6": [R("faa_general", "Logic gates: AND, OR, NOT, NAND, NOR, XOR and XNOR", 177)],
}
for _tid, _refs in FAA_REFS.items():
    REFS.setdefault(_tid, []).extend(_refs)
