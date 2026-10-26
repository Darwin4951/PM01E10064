# Guía para explicar mi aplicación Gestión de Contactos

## Introducción

Mi aplicación se llama **Gestión de Contactos** y fue creada en Android Studio. Utilicé Java para programar las funciones, XML para diseñar las pantallas y SQLite para guardar los contactos.

La aplicación permite guardar, editar, eliminar, compartir y llamar a los contactos.

## Pantalla 1: Registrar contactos

En esta pantalla se pueden ingresar los siguientes datos:

- País
- Nombre
- Teléfono
- Nota
- Fotografía

**Funciones principales:**

- `onCreate()`: inicia la pantalla y carga los elementos.
- `guardar()`: revisa que los datos estén correctos y guarda el contacto.
- `elegirImagen()`: permite seleccionar una foto de la galería o tomarla con la cámara.
- `mostrarImagen()`: muestra la fotografía del contacto.

También se puede utilizar esta pantalla para modificar un contacto que ya está guardado.

## Pantalla 2: Lista de contactos

En esta pantalla aparecen todos los contactos registrados en la base de datos.

**Funciones principales:**

- `refrescar()`: actualiza la lista de contactos.
- `editarContacto()`: permite cambiar la información de un contacto.
- `eliminar()`: elimina un contacto después de confirmar.
- `compartir()`: permite enviar el nombre y teléfono por otras aplicaciones.
- `verImagen()`: muestra la fotografía del contacto.

Para mostrar los contactos utilicé un `ListView`, que permite verlos en forma de lista.

## Pantalla 3: Realizar llamadas

Desde la lista se puede seleccionar un contacto y presionar el botón para llamar.

Primero aparece un mensaje preguntando si deseamos realizar la llamada.

- `confirmarLlamada()`: muestra el mensaje de confirmación.
- `responderPermiso()`: comprueba si se autorizó el permiso para llamar.
- `llamar()`: realiza la llamada utilizando `ACTION_CALL`.

Si el usuario cancela, la llamada no se realiza.

## Base de datos SQLite

Utilicé SQLite para guardar los contactos dentro del teléfono.

La base de datos se llama `DBContactos` y tiene una tabla llamada `contactos`.

La clase `Contactos` contiene los datos de cada persona y `SQLiteConexion` permite realizar las siguientes operaciones:

- Guardar contactos.
- Mostrar los contactos guardados.
- Buscar contactos.
- Actualizar información.
- Eliminar contactos.

## Diseño de la aplicación

Utilicé colores verde oscuro, verde claro y blanco.

El diseño es sencillo y tiene botones para guardar, editar, eliminar, compartir y llamar.

## Conclusión

Con este proyecto puse en práctica lo aprendido sobre Java, XML, SQLite y Android Studio.

Aprendí a crear formularios, guardar información en una base de datos, mostrar listas y utilizar funciones del teléfono, como la cámara y las llamadas.

La aplicación permite administrar contactos de una manera sencilla.
