import json
import sys
from pytrends.request import TrendReq

def get_trends(geo="RU", count=10):
    try:
        pytrends = TrendReq(hl="ru-RU", tz=360)

        trending = pytrends.trending_searches(pn=geo)
        topics = []

        for i, row in trending.head(count).iterrows():
            topic = row[0] if hasattr(row, '__getitem__') else str(row)
            topics.append({
                "topic": topic,
                "category": "google_trends",
                "source": "google_trends",
                "viral_score": max(7, 10 - i)
            })

        return {"topics": topics}
    except Exception as e:
        return {"topics": [], "error": str(e)}

if __name__ == "__main__":
    geo = sys.argv[1] if len(sys.argv) > 1 else "RU"
    count = int(sys.argv[2]) if len(sys.argv) > 2 else 10
    result = get_trends(geo, count)
    print(json.dumps(result, ensure_ascii=False))
