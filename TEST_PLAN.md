# Plan de Pruebas — Proyecto CvLAC Extractor

## Índice
- Resumen ejecutivo
- Alcance y supuestos
- Documentos y artefactos de referencia
- Estrategia de pruebas
- Criterios de entrada / salida
- Ambiente, herramientas y comandos
- Matriz de trazabilidad
- Plantillas (Caso de prueba, Bug, Registro de ejecución)
- Casos de prueba ejemplo (detallados)
- Plan de ejecución y cronograma
- Roles y responsabilidades
- Métricas y criterios de calidad
- Riesgos y mitigaciones
- Entregables
- Recomendaciones y próximos pasos
- Queries de validación SQLite (ejemplos)

---

## Resumen ejecutivo
Objetivo: Verificar que la herramienta de extracción de CVs (CvLAC extractor) se instala, funciona y se utiliza correctamente según la documentación de implementación, funcionamiento y uso; asegurar calidad funcional, integridad de datos, estabilidad y rendimiento aceptable.

---

## Alcance y supuestos
Alcance:
- Tests de compilación/instalación.
- Tests funcionales automatizados (Cucumber + Selenium).
- Verificaciones de persistencia en SQLite.
- Pruebas de rendimiento básicas y pruebas de regresión.

Supuestos:
- Entorno principal: Windows (se proporcionan comandos `cmd.exe`).
- Chrome instalado y `chromedriver.exe` en el repo (`CvLACExtractorData/chromedriver.exe`).
- Proyecto ejecutable con `gradlew.bat` desde la raíz `CvLACExtractorData`.
- Los features clave se encuentran en `src/test/resources/features/ReserchearsCVExtractor.feature`.

Archivos/elementos clave (referenciados en este plan):
- `build.gradle`
- `gradlew.bat`
- `CvLACExtractorData/chromedriver.exe`
- `src/test/resources/features/ReserchearsCVExtractor.feature`
- `src/test/java/data/SQLiteManager.java`
- `src/test/java/runner/RunnerCucumber.java`

---

## Documentos y artefactos de referencia
- Código fuente y pruebas: `src/test/java` y `src/test/resources/features`
- Feature principal: `ReserchearsCVExtractor.feature`
- Driver y setup: `DriverSetUp.java`, `chromedriver.exe`
- DB de prueba: `CVLACData.db`, `cvlacdb.db`, `localDB.db`
- Informes generados por Gradle: `build/reports/tests/`

---

## Estrategia de pruebas
Tipos de pruebas a ejecutar:
- Pruebas de despliegue/compilación (build).
- Smoke tests (flujo crítico).
- Pruebas funcionales (Cucumber + Selenium).
- Pruebas de integración (UI ↔ DB SQLite).
- Pruebas de aceptación (criterios de negocio).
- Pruebas de regresión (suite automatizada).
- Pruebas de rendimiento/escala (extracción en lote).
- Compatibilidad de `chromedriver` vs Chrome.
- Pruebas de documentación/usabilidad (verificar pasos de uso descritos).

Cobertura objetivo:
- 100% de los escenarios definidos en `ReserchearsCVExtractor.feature` y las funciones críticas de `SQLiteManager`.

---

## Criterios de entrada y salida

Criterios de entrada:
- Código compila sin errores (`.\gradlew.bat clean build`).
- Chrome y `chromedriver.exe` compatibles y accesibles.
- DBs de prueba disponibles o copias limpias preparadas.
- Acceso a la máquina Windows de pruebas.

Criterios de salida:
- Smoke tests y pruebas críticas (funcionales & integración) pasan.
- No hay defectos de severidad alta abiertos sin plan de mitigación.
- Reportes generados y verificados (build/reports/tests).

---

## Ambiente de pruebas y herramientas
- OS: Windows 10/11 (cmd.exe).
- Java JDK compatible con `build.gradle`.
- Gradle wrapper: `gradlew.bat` (usar desde la raíz `CvLACExtractorData`).
- Chrome (versión compatible con `chromedriver.exe`).
- chromedriver: `CvLACExtractorData/chromedriver.exe`.
- SQLite (cliente opcional para verificar DBs).
- Frameworks: Cucumber, Selenium WebDriver, JUnit (según proyecto).
- IDE: IntelliJ IDEA (opcional).

Comandos (ejecutar en `CvLACExtractorData` desde `cmd.exe`):

```cmd
:: Limpiar y ejecutar todos los tests
.\gradlew.bat clean test

:: Ejecutar solo la suite que contenga RunnerCucumber (si aplica)
.\gradlew.bat test --tests "*RunnerCucumber*"

:: Ejecutar pruebas y generar reportes (gradle genera en build/reports/tests)
.\gradlew.bat clean test
```

Nota: Si necesitas modo headless, recomiendo exponer una variable de sistema (por ejemplo `-Dheadless=true`) y ajustar `DriverSetUp` para leerla.

---

## Matriz de trazabilidad (resumen)
- `src/test/resources/features/ReserchearsCVExtractor.feature` -> Pruebas funcionales Cucumber + Smoke.
- `src/test/java/data/SQLiteManager.java` -> Pruebas de integración y validación de persistencia.
- `CvLACExtractorData/chromedriver.exe` & `DriverSetUp.java` -> Pruebas de compatibilidad / configuración.
- `src/test/java/runner/RunnerCucumber.java` -> Ejecución y automatización de suites.

---

## Plantillas

Plantilla: Caso de Prueba (TP-YYYY-NNN)
- ID: TP-FUNC-001
- Título:
- Objetivo:
- Precondiciones:
- Datos de prueba:
- Pasos:
- Resultado esperado:
- Resultado real:
- Estado: Pass / Fail
- Severidad / Prioridad:
- Observaciones / logs:
- Evidencia: captura, reporte, query DB.

Plantilla: Bug
- ID:
- Resumen:
- Entorno:
- Pasos para reproducir:
- Resultado esperado:
- Resultado real:
- Severidad:
- Prioridad:
- Adjuntos: logs, capturas, DB dump.

Plantilla: Registro de Ejecución
- Suite:
- Tester:
- Fecha/Hora:
- Tests ejecutados / total:
- Pass / Fail counts:
- Observaciones:
- Próximos pasos / bloqueos:

---

## Casos de prueba ejemplo (detallados)

TP-DEPLOY-001 — Verificar compilación del proyecto
- Objetivo: El proyecto compila sin errores.
- Precondiciones: JDK instalado.
- Pasos:
  1. Abrir `cmd.exe` en `CvLACExtractorData`.
  2. Ejecutar `.\gradlew.bat clean build`.
- Resultado esperado: Build exitoso, clases generadas en `build/` o `target/`.

TP-SMOKE-001 — Flujo principal (smoke)
- Objetivo: Validar flujo: abrir home > seleccionar sistema CVLac > seleccionar nivel educativo > extraer CVs.
- Precondiciones: Chrome instalado, `chromedriver.exe` accesible, DB de prueba lista.
- Pasos:
  1. Ejecutar `.\gradlew.bat test` (o la suite que ejecute `ReserchearsCVExtractor.feature`).
- Resultado esperado:
  - Navegador abre home.
  - Interacciones completadas sin excepciones.
  - Inserción/registro en DB confirmada.

TP-FUNC-002 — Verificar persistencia en SQLite
- Objetivo: Verificar que los datos extraídos se guardan correctamente en `CVLACData.db`.
- Pasos:
  1. Ejecutar el escenario de extracción.
  2. Abrir `CVLACData.db` con cliente SQLite.
  3. Ejecutar queries de verificación.
- Resultado esperado: Registros esperados con campos clave no nulos.

TP-INT-001 — Integración UI <-> DB (coherencia)
- Objetivo: Para un perfil buscado ("Juan Perez"), los datos mostrados en UI coinciden con los almacenados en DB.
- Pasos:
  1. Ejecutar búsqueda para "Juan Perez".
  2. Capturar datos de UI.
  3. Consultar DB y comparar.
- Resultado esperado: Coincidencia 100% en campos clave.

TP-PERF-001 — Rendimiento de extracción masiva
- Objetivo: Medir tiempo para extraer 100 perfiles consecutivos.
- Métricas de aceptación:
  - Tiempo promedio por perfil < 5s (ajustable).
  - Tasa de error < 2%.

TP-REG-001 — Regresión básica
- Objetivo: Ejecutar suite completa después de un cambio en `DriverSetUp` o `SQLiteManager`.
- Resultado esperado: No regresiones en tests críticos.

TP-COMP-001 — Compatibilidad Chrome/Driver
- Objetivo: Verificar `chromedriver.exe` con la versión de Chrome instalada.
- Pasos:
  1. Validar versión de Chrome.
  2. Ejecutar smoke tests.
- Resultado esperado: Tests pasan o se documentan incompatibilidades.

---

## Plan de ejecución y cronograma (ejemplo)
- Día 0.5: Preparación de ambiente (instalación JDK, chequeo Chrome/Driver, preparar DB).
- Día 1: Compilación y Smoke tests; corrección blockers.
- Día 2–3: Pruebas funcionales e integración (Cucumber + validación DB).
- Día 4: Pruebas de rendimiento y compatibilidad.
- Día 5: Consolidación de reportes y aceptación.

---

## Roles y responsabilidades
- QA Lead: Coordinar ejecución y priorizar casos.
- Tester(s): Ejecutar pruebas y reportar bugs.
- Dev: Corregir issues y aclarar dudas.
- PO / Stakeholder: Aceptación final.

---

## Métricas y criterios de calidad
- % de tests pass (objetivo: >95% no críticos; 100% críticos).
- Tiempo promedio por extracción.
- Severidad de defectos abiertos (0 severidades altas sin plan).
- Cobertura de pruebas de features críticas.

---

## Riesgos y mitigaciones
- Riesgo: incompatibilidad `chromedriver`/Chrome. Mitigación: incluir script/nota para validar versiones y mantener driver actualizado.
- Riesgo: Datos reales sensibles. Mitigación: usar DB de pruebas con datos ficticios.
- Riesgo: Tests UI frágiles por cambios DOM. Mitigación: usar page objects y selectores robustos (`CvLacHome.java`).
- Riesgo: Entorno de CI diferente a local. Mitigación: usar modo headless y validar dependencias en pipeline.

---

## Entregables
- Este Plan de Pruebas (Markdown).
- Suite automatizada ejecutable con `.\gradlew.bat test`.
- Reportes de ejecución (en `build/reports/tests`).
- Plantillas para bug y registro de ejecución.
- Scripts opcionales para preparar/limpiar DB (si se piden).

---

## Recomendaciones prácticas
- Ajustar `DriverSetUp` para soportar `-Dheadless=true` y parámetro de timeout configurable.
- Añadir script de verificación de versión de Chrome y selector del driver adecuado.
- Integrar pruebas en CI (GitHub Actions / Jenkins) con pasos reproducibles:
  - Setup JDK -> Ejecutar `.\gradlew.bat clean test` -> Archivar `build/reports/tests`.
- Crear scripts para preparar/limpiar DB antes/después de cada ejecución automatizada.

---

## Próximos pasos (opciones que puedo realizar ahora)
Elige una o varias opciones y lo hago:
1. Generar un CSV con la lista completa de casos de prueba.
2. Crear un script Java/JUnit pequeño que ejecute una extracción y valide la DB (automático).
3. Generar un `README.md` de pruebas con pasos reproducibles y comandos.
4. Crear scripts para validar versión de Chrome y ejecutar el driver correcto.
5. Convertir las plantillas a archivos (CSV/MD) dentro del repo.

---

## Queries de validación SQLite (ejemplos)

```sql
-- Contar registros
SELECT COUNT(*) FROM researchers;

-- Encontrar registros con campos nulos importantes
SELECT id, name, education_level FROM researchers WHERE name IS NULL OR education_level IS NULL;
```

---

*Documento generado el 11 de febrero de 2026.*

