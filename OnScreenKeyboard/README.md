# On-Screen QWERTY Keyboard

Fabric client mod for Minecraft 1.21.11.

## Versions
- Minecraft: 1.21.11
- Fabric Loader: 0.19.5
- Fabric API: 0.141.6+1.21.11
- Java: 21

## Controls
- **K** toggles the floating keyboard.
- This is also the key to map in Mojo Launcher's custom controls: map your on-screen control to the **K** key.
- The physical K key is consumed by the mod so it does not accidentally type `k` when it is being used as the toggle.

## Keyboard pages
### Page 1
`q w e r t y u i o p`
`a s d f g h j k l`
`⬆ z x c v b n m ⌫`
`123 SPACE . ↵`

### Page 2
`1 2 3 4 5 6 7 8 9 0`
`@ # £ _ & - + ( ) /`
`🔱 * " ' : ; ! ? ⌫`
`ABC , SPACE . ↵`

### Page 3
`~ \` | • √ π ÷ × § ∆`
`€ ¥ $ ¢ ^ ° = { } \\`
`123 % © ® ™ ✓ [ ] ⌫`
`ABC < SPACE > ↵`

The keyboard targets the currently focused Minecraft text field. Enter is forwarded to the original screen so chat and other text-entry screens can complete/submit typing normally.

## Build
Use the included GitHub Actions workflow, or run Gradle with Java 21:

```powershell
gradle build
```

The compiled jar is created in `build/libs/`.
