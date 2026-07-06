import asyncio
import edge_tts
import sys

async def generate_tts(text, voice, rate, output_path):
    communicate = edge_tts.Communicate(text, voice, rate=rate)
    await communicate.save(output_path)

if __name__ == "__main__":
    if len(sys.argv) != 5:
        print("Usage: edge_tts_wrapper.py <text> <voice> <rate> <output_path>")
        sys.exit(1)

    text = sys.argv[1]
    voice = sys.argv[2]
    rate = sys.argv[3]
    output_path = sys.argv[4]

    asyncio.run(generate_tts(text, voice, rate, output_path))
