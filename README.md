# Sismo Offline 🌎📱

Proyecto escolar de demostración con **Jetpack Compose + acelerómetro + Bluetooth clásico**.

## Compatibilidad solicitada

- `compileSdk = 33`
- `targetSdk = 33`
- Funciona en Android 12L / API 32 y Android 13 / API 33.
- `minSdk = 26`, así que también puede ejecutarse en versiones anteriores que tengan Bluetooth clásico y acelerómetro.
- No requiere Internet, servidor, cuenta ni GPS.

La configuración usa Kotlin 1.9.10 + Compose Compiler 1.5.3, una combinación compatible oficialmente. Compose Compiler 1.5.3 corresponde a Kotlin 1.9.10.

## Cómo funciona

Los dos teléfonos instalan **la misma APK**.

**Teléfono A: Sensor**

1. Se empareja con el teléfono B desde Ajustes > Bluetooth.
2. En la app se selecciona `📦 Sensor`.
3. Se pulsa `Iniciar detector`.
4. El acelerómetro mide el movimiento del teléfono.
5. El detector elimina aproximadamente la gravedad mediante un filtro y busca varias muestras fuertes consecutivas.
6. Cuando se confirma un evento, envía `EARTHQUAKE|valor` por Bluetooth RFCOMM.

**Teléfono B: Monitor**

1. Se empareja con A desde Ajustes > Bluetooth.
2. En la app se selecciona `📱 Monitor`.
3. Se actualizan los teléfonos y se selecciona A.
4. Se pulsa `Conectar con sensor`.
5. Al recibir `EARTHQUAKE|valor`, la app muestra la pantalla de alerta.

## Prueba rápida para la exposición

1. Empareja los teléfonos en los ajustes de Bluetooth.
2. En A: `Sensor` > `Iniciar detector`.
3. En B: `Monitor` > `Actualizar teléfonos` > selecciona A > `Conectar con sensor`.
4. Pulsa `⚡ Simular temblor` en el teléfono sensor.
5. B debe abrir inmediatamente la pantalla de alerta.
6. Después repite sacudiendo físicamente la caja para demostrar el acelerómetro.

## Bluetooth en Android 12/13

Como la app usa teléfonos que ya están emparejados, solicita `BLUETOOTH_CONNECT` en Android 12/API 31 o superior. No usa escaneo Bluetooth desde la app, por lo que no necesita `BLUETOOTH_SCAN` para este diseño.

## Archivos principales

```text
app/src/main/java/com/example/sismooffline/
├── MainActivity.kt
├── core/
│   ├── BluetoothManager.kt
│   └── EarthquakeDetector.kt
└── ui/
    ├── SismoApp.kt
    ├── MainScreen.kt
    ├── AlertScreen.kt
    └── Components.kt
```

## Nota científica

Esto es una **detección experimental de movimiento**, no un sismómetro profesional ni un sistema de predicción sísmica. La cifra mostrada es una magnitud de aceleración del teléfono, no la magnitud sísmica de un terremoto.
