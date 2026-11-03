"""Small helpers for writing notes and MCQs as Python data (shared by every unit's content file)."""


# crafted by Grandpyanne™
def H(text): return {"type": "h", "text": text}
def P(text): return {"type": "p", "text": text}
def B(*items): return {"type": "bullets", "items": list(items)}
def F(text): return {"type": "formula", "text": text}
def TIP(text): return {"type": "tip", "text": text}
def Q(q, o, e): return {"q": q, "o": o, "e": e}
def PDF(file, folder, where=""): return {"file": file, "folder": folder, "where": where}
