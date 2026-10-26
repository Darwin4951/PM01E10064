# Guía para explicar Gestión de contactos

La aplicación usa Java para las acciones, XML para las pantallas y SQLite para guardar los contactos en el teléfono. La base sigue llamándose `DBContactos`, con la tabla `contactos` y su identificador `id`.

## Pantalla 1: registrar y editar

`MainActivity` muestra país, nombre, teléfono, nota y fotografía. Se usa el mismo formulario para registrar y editar.

- `onCreate()`: conecta los componentes XML, prepara el selector de país y carga desde SQLite el contacto cuando se abre en modo edición.
- `guardar()`: comprueba los campos obligatorios, las letras del nombre y los dígitos del teléfono. Si se está editando, actualiza por ID; en caso contrario, inserta un contacto nuevo.
- `elegirImagen()`: permite escoger galería o cámara.
- `cargarGaleria()` y `establecerImagen()`: leen y reducen la foto para guardarla como texto Base64. Base64 representa los bytes de la imagen mediante caracteres; no es una dirección de archivo.
- `mostrarImagen()`: muestra la foto guardada sin reemplazar el texto original. Así se conserva si solo cambia el nombre o el teléfono.

## Pantalla 2: lista de contactos

`ActivityLista` usa un `ListView`. Su adaptador toma cada objeto `Contactos` y coloca sus datos en `item_contacto.xml`. `getView()` prepara una fila y asigna las acciones al contacto de esa fila.

- `refrescar()`: consulta SQLite y actualiza el adaptador. `onResume()` lo ejecuta cada vez que se regresa a la lista.
- `editarContacto()`: abre el formulario con el ID y el indicador de edición. No envía la fotografía en el Intent.
- `eliminar()`: muestra la confirmación y, si se acepta, elimina únicamente el ID seleccionado y refresca la lista.
- `compartir()`: usa `ACTION_SEND`, tipo `text/plain` y `Intent.createChooser()` para compartir nombre y número.
- `imagen()`: convierte Base64 en un `Bitmap`, reduciendo su tamaño según la vista para evitar consumir memoria innecesariamente.
- `verImagen()`: muestra esa fotografía en un diálogo ampliado; avisa si no hay una imagen disponible.

## Pantalla 3: llamar

La llamada se realiza desde la opción **Llamar** de la lista; no necesita otra Activity.

1. `confirmarLlamada()` muestra nombre y número en un diálogo con **Sí** y **No**.
2. Al aceptar, comprueba `CALL_PHONE`. Si falta, solicita el permiso mediante el mecanismo de resultados de Android.
3. `responderPermiso()` continúa solo si se concedió el permiso y el ID y número siguen correspondiendo al contacto confirmado.
4. `llamar()` vuelve a comprobar el permiso, verifica que exista una aplicación compatible y abre `ACTION_CALL` con `tel:`. Captura excepciones para evitar cierres inesperados.

`NumeroTelefonico.completo()` obtiene el código a partir del signo `+` del país usando un ciclo `for`. Combina código y teléfono, respetando los prefijos internacionales ya incluidos. El catálogo actual tiene números nacionales de ocho dígitos.

## Clases y métodos de datos

`Contactos` reúne ID, país, nombre, teléfono, nota e imagen. Sus métodos `get` permiten leerlos y sus métodos `set` permiten cambiarlos.

`SQLiteConexion` hereda de `SQLiteOpenHelper` y centraliza las operaciones:

- `insertarContacto()`: crea una fila.
- `obtenerContactos()`: devuelve los contactos ordenados por nombre.
- `obtenerContacto(id)`: busca una sola fila.
- `actualizarContacto()`: modifica la fila identificada por ID.
- `eliminarContacto(id)`: elimina la fila identificada por ID.

Las condiciones `id = ?` reciben el identificador como argumento. La edición no inserta una nueva fila y la base mantiene su nombre, versión y estructura.

## Colores y comprobación manual

`colors.xml` define verde oscuro `#2E6B4E`, verde claro `#E8F3EB` y blanco. Los XML usan esos recursos para mantener los mismos colores en formularios, lista, botones e iconos.

Para la evaluación: registrar un contacto con foto, verlo en la lista, editar solo su nombre y comprobar que conserva la foto; cancelar una eliminación y luego confirmarla; compartir; abrir la imagen; cancelar una llamada y probar los casos de permiso concedido y denegado. Una llamada real debe comprobarse en un dispositivo con capacidad telefónica.
