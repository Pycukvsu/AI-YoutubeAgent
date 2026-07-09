import json
import os
from http.server import HTTPServer, BaseHTTPRequestHandler
from urllib.parse import urlparse, parse_qs
import webbrowser

CLIENT_ID = os.environ.get("YOUTUBE_CLIENT_ID", "YOUR_CLIENT_ID")
CLIENT_SECRET = os.environ.get("YOUTUBE_CLIENT_SECRET", "YOUR_CLIENT_SECRET")
REDIRECT_URI = "http://localhost:8080/callback"
SCOPES = "https://www.googleapis.com/auth/youtube.upload https://www.googleapis.com/auth/youtube.readonly"

auth_url = (
    f"https://accounts.google.com/o/oauth2/v2/auth?"
    f"client_id={CLIENT_ID}&"
    f"redirect_uri={REDIRECT_URI}&"
    f"response_type=code&"
    f"scope={SCOPES}&"
    f"access_type=offline&"
    f"prompt=consent"
)

print("=" * 60)
print("YouTube OAuth2 Setup")
print("=" * 60)
print()
print("1. Открой эту ссылку в браузере:")
print()
print(auth_url)
print()
print("2. Авторизуйся и скопируй код из URL")
print("3. Вставь код сюда:")
print()

code = input("Authorization code: ").strip()

import urllib.request
import urllib.parse

data = urllib.parse.urlencode({
    "code": code,
    "client_id": CLIENT_ID,
    "client_secret": CLIENT_SECRET,
    "redirect_uri": REDIRECT_URI,
    "grant_type": "authorization_code"
}).encode()

req = urllib.request.Request("https://oauth2.googleapis.com/token", data=data)
response = urllib.request.urlopen(req)
tokens = json.loads(response.read())

print()
print("=" * 60)
print("SUCCESS! Сохрани эти данные:")
print("=" * 60)
print()
print(f"YOUTUBE_REFRESH_TOKEN={tokens['refresh_token']}")
print()
print("Access token (обновляется автоматически):")
print(f"  expires_in: {tokens['expires_in']} секунд")
print()
print("=" * 60)
