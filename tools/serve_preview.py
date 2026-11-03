#!/usr/bin/env python3
"""Serve the self-contained structure preview on localhost; no game is launched."""
import argparse
from functools import partial
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--port', type=int, default=0, help='0 selects an available local port')
args = parser.parse_args()
directory = Path(__file__).resolve().parents[1] / 'preview'
handler = partial(SimpleHTTPRequestHandler, directory=str(directory))
server = ThreadingHTTPServer(('127.0.0.1', args.port), handler)
print('http://127.0.0.1:' + str(server.server_port) + '/', flush=True)
try:
    server.serve_forever()
except KeyboardInterrupt:
    pass
finally:
    server.server_close()
