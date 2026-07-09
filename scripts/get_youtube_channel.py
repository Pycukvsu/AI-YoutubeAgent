import json
import urllib.request
import urllib.parse

REFRESH_TOKEN = input("Вставь YOUTUBE_REFRESH_TOKEN: ").strip()
CLIENT_ID = input("Вставь YOUTUBE_CLIENT_ID: ").strip()
CLIENT_SECRET = input("Вставь YOUTUBE_CLIENT_SECRET: ").strip()

data = urllib.parse.urlencode({
    "client_id": CLIENT_ID,
    "client_secret": CLIENT_SECRET,
    "refresh_token": REFRESH_TOKEN,
    "grant_type": "refresh_token"
}).encode()

req = urllib.request.Request("https://oauth2.googleapis.com/token", data=data)
response = urllib.request.urlopen(req)
tokens = json.loads(response.read())
access_token = tokens["access_token"]

req = urllib.request.Request(
    "https://www.googleapis.com/youtube/v3/channels?part=snippet&mine=true",
    headers={"Authorization": f"Bearer {access_token}"}
)
response = urllib.request.urlopen(req)
result = json.loads(response.read())

if result.get("items"):
    channel = result["items"][0]
    channel_id = channel["id"]
    title = channel["snippet"]["title"]
    print()
    print("=" * 60)
    print(f"Канал: {title}")
    print(f"YOUTUBE_CHANNEL_ID={channel_id}")
    print("=" * 60)
else:
    print("Канал не найден. Убедись что аккаунт YouTube создал канал.")
