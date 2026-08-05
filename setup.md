# Setup — do this before class

Total time: about ten minutes. If anything fails, bring the error to class — the first
fifteen minutes include a checkpoint where we fix setup problems together.

## 1. Pick your language track

The hands-on lab is the same in all three languages. Pick the one you're most comfortable
in; you only need **one**.

| Track | You need | Check with |
|---|---|---|
| Java | Java 21+ (Gradle comes with the project) | `java -version` |
| Python | Python 3.11+ | `python3 --version` |
| TypeScript | Node 20+ | `node --version` |

## 2. Clone the course repository

```bash
git clone https://github.com/kousen/agentic-commerce.git
cd agentic-commerce
```

## 3. Install your track's dependencies

**Java** — nothing to install; the Gradle wrapper handles it.

**Python:**

```bash
cd labs/python
python3 -m venv .venv && source .venv/bin/activate   # Windows: .venv\Scripts\activate
pip install -r requirements.txt
```

**TypeScript:**

```bash
cd labs/typescript
npm install
```

## 4. Run the checkpoint

This makes one call to the hosted course platform (MockHub) and prints a line.

| Track | Command |
|---|---|
| Java | `cd labs/java && ./gradlew checkpoint` |
| Python | `cd labs/python && pytest -k checkpoint` |
| TypeScript | `cd labs/typescript && npm run checkpoint` |

You should see:

```
CHECKPOINT OK — <your track> — https://mockhub.kousenit.com
```

**During class**, you'll run this again at the 15-minute mark and paste that line into
the chat. That's the whole checkpoint — it exists so any setup problem surfaces ninety
minutes before the lab needs your setup to work.

## Nothing else is required

- No accounts, no API keys, no Docker, no database.
- The lab runs against the hosted MockHub instance over HTTPS.
- An IDE helps but any editor works; the lab is a single test file in your track.
