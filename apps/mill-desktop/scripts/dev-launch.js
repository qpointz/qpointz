const http = require("http");
const { execFileSync, spawn } = require("child_process");
const path = require("path");

const ports = [5174, 5175, 5176, 5177, 5178, 5179, 5180, 5173];
const timeout = 30000;
const start = Date.now();

function isViteServer(port) {
  return new Promise((resolve) => {
    const req = http.get(`http://localhost:${port}/@vite/client`, (res) => {
      let body = "";
      res.on("data", (c) => { body += c; });
      res.on("end", () => {
        req.destroy();
        resolve(res.statusCode === 200 && body.length > 100);
      });
    });
    req.on("error", () => resolve(false));
    req.setTimeout(1000, () => { req.destroy(); resolve(false); });
  });
}

async function main() {
  let foundPort = null;
  while (Date.now() - start < timeout) {
    for (const port of ports) {
      if (await isViteServer(port)) {
        foundPort = port;
        break;
      }
    }
    if (foundPort) break;
    await new Promise((r) => setTimeout(r, 500));
  }

  if (!foundPort) {
    process.stderr.write("Timed out waiting for Vite dev server\n");
    process.exit(1);
  }

  console.log(`Vite detected on port ${foundPort}, launching Electron...`);

  const electronPath = require("electron");
  const mainJs = path.resolve(__dirname, "..", "dist-electron", "electron", "main.js");

  const child = spawn(electronPath, [mainJs], {
    stdio: "inherit",
    env: { ...process.env, VITE_DEV_SERVER_URL: `http://localhost:${foundPort}` }
  });

  child.on("exit", (code) => process.exit(code ?? 0));
}

main();
