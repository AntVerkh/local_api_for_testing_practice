# Users API - Installation Guide

## Quick Installation

1. **Download** the entire project to any folder on your computer
2. **Right-click** on `install.bat` and select **"Run as administrator"**
3. Follow the on-screen instructions
4. After installation, double-click the **"Users API"** shortcut on your desktop

## Features

- ✅ **Automatic** - Installs everything needed
- ✅ **Portable** - Works from any folder
- ✅ **No admin rights required** after installation
- ✅ **Self-contained** - Includes Java and all dependencies
- ✅ **Desktop shortcut** - Easy to start

## What gets installed:

- Java 17 (if not already installed)
- Maven Wrapper (for building)
- All project dependencies
- Desktop shortcut
- Startup scripts

## Manual Start (if needed)

If the desktop shortcut doesn't work, you can:
1. Open the project folder
2. Double-click `start-api.bat`

## Access Points:

- **UI**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console
- **Credentials**:
  - Admin: `admin` / `admin`
  - User: `peasant` / `peasant`

## Troubleshooting

**If installation fails:**
- Make sure you have internet connection
- Run as Administrator
- Check if the folder contains `pom.xml`

**If application doesn't start:**
- Check if port 8080 is available
- Try running `start-api.bat` manually
- Check Windows Firewall settings

**To uninstall:**
- Run `uninstall.bat` from project folder
- Or manually delete the desktop shortcut
