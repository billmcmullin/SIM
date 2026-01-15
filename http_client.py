# http_client.py
import requests
from typing import Optional, Dict, Any, List

class HTTPClient:
    def __init__(self, base: str, headers: Optional[Dict[str,str]] = None, verify=True):
        self.base = base.rstrip("/")
        self.session = requests.Session()
        self.headers = headers or {}
        self.session.verify = verify

    def _make_url(self, path: str) -> str:
        if path.startswith("http://") or path.startswith("https://"):
            return path
        if not path.startswith("/"):
            path = "/" + path
        return self.base + path

    def get(self, path: str, params: Dict[str,Any]=None, timeout: int=30):
        url = self._make_url(path)
        r = self.session.get(url, headers=self.headers, params=params, timeout=timeout)
        r.raise_for_status()
        return r

    def post(self, path: str, json_payload: Dict[str,Any]=None, timeout: int=60):
        url = self._make_url(path)
        r = self.session.post(url, headers={**self.headers, "Content-Type":"application/json"}, json=json_payload, timeout=timeout)
        r.raise_for_status()
        return r

    def fetch_embeds(self) -> List[Dict[str,Any]]:
        r = self.get("/api/v1/embed")
        try:
            data = r.json()
        except Exception:
            return []
        if isinstance(data, dict):
            for k in ("embeds","data","items","results"):
                if k in data and isinstance(data[k], list):
                    return data[k]
            for v in data.values():
                if isinstance(v, list):
                    return v
            return []
        if isinstance(data, list):
            return data
        return []

    def fetch_chats_for_embed(self, embed_uuid: str) -> List[Dict[str,Any]]:
        r = self.get(f"/api/v1/embed/{embed_uuid}/chats")
        try:
            data = r.json()
        except Exception:
            return []
        if isinstance(data, dict):
            for k in ("chats","items","data","results","history"):
                if k in data and isinstance(data[k], list):
                    return data[k]
            return []
        if isinstance(data, list):
            return data
        return []
