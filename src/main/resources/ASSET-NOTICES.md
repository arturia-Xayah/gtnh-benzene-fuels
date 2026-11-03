# Texture sources

Native GregTech and TecTech textures are extracted without pixel changes from GT5-Unofficial 5.09.54.205 for the local structure preview.
Source: https://github.com/GTNewHorizons/GT5-Unofficial/tree/5.09.54.205/src/main/resources/assets
Project license: LGPL-3.0; the addon includes its LICENSE. Exact resource paths and SHA-256 values are recorded in design/generator-material-palette.json.

The new resonance_light_casing.png is derived from GregTech iconsets/MACHINE_DIM_INJECTOR.png: recolored to blue-white, scaled with nearest-neighbor filtering and given a frame, bolts and luminous cross-channel. Its editable generator is tools/select_structure_materials.py.

Minecraft iron-bar artwork is used solely by the local preview; the game uses the installed vanilla block. No vanilla texture is redistributed inside the addon JAR.

Controller art: tools/design_controller_texture.py draws 64x64 transparent idle/active front overlays and separate emissive masks. Its background is an unchanged nearest-neighbor enlargement of MACHINE_CASING_CHEMICALLY_INERT.png, preserving the outer 8px. OVERLAY_FRONT_DIESEL_ENGINE(_ACTIVE).png and LARGETURBINE_NEW(5/_ACTIVE5).png are visual references only; their controller graphics are not copied. Native casing remains the future side/top/back texture. Provenance and hashes: verification/controller-texture.json. The 0.3.0 controller uses these layered textures at runtime. Light casing metadata 0/1 select idle/active art and light values 0/15.
