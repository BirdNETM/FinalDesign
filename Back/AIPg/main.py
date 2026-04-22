import subprocess
import sys
import os
import yaml
def start_service(name, cfg):
    print(f"[START] {name} on {cfg['host']}:{cfg['port']}")
    cmd = [
        sys.executable, "-m", "uvicorn",
        cfg["module"],
        "--host", cfg["host"],
        "--port", str(cfg["port"])
    ]
    if cfg.get("reload"):
        cmd.append("--reload")

    # 指定 cwd 为项目根目录
    cwd = os.path.dirname(os.path.abspath(__file__))

    subprocess.Popen(cmd, cwd=cwd, stdout=sys.stdout, stderr=sys.stderr)

def main():
    with open("config.yaml", "r", encoding="utf-8") as f:
        config = yaml.safe_load(f)

    for name, svc in config["services"].items():
        start_service(name, svc)

    print("All services started.")
    input("Press ENTER to stop all services...")

if __name__ == "__main__":
    main()