package com.dbarragan.comandafacil.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ComandaFacilDatabase(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREAR_PRODUCTOS)
        db.execSQL(SQL_CREAR_JORNADAS)
        db.execSQL(SQL_CREAR_VENTAS)
        db.execSQL(SQL_CREAR_DETALLE_VENTAS)
        db.execSQL(SQL_CREAR_GASTOS)
        db.execSQL(SQL_CREAR_MOVIMIENTOS)
        db.execSQL(SQL_CREAR_CONFIGURACION)
        db.execSQL("INSERT INTO configuracion (id) VALUES (1)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Estrategia de migración para versiones futuras del esquema
    }

    override fun onConfigure(db: SQLiteDatabase) {
        // Habilita claves foráneas en SQLite
        db.setForeignKeyConstraintsEnabled(true)
    }

    companion object {
        const val DATABASE_NAME = "comandafacil.db"
        const val DATABASE_VERSION = 1

        private val SQL_CREAR_PRODUCTOS = """
            CREATE TABLE productos (
                id                   INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre               TEXT    NOT NULL,
                costo_ingredientes   REAL    NOT NULL DEFAULT 0,
                costo_fijo_prorrateado REAL  NOT NULL DEFAULT 0,
                margen_ganancia      REAL    NOT NULL DEFAULT 30,
                precio_sugerido      REAL    NOT NULL DEFAULT 0,
                stock_actual         REAL    NOT NULL DEFAULT 0,
                stock_minimo         REAL    NOT NULL DEFAULT 0,
                activo               INTEGER NOT NULL DEFAULT 1,
                fecha_creacion       TEXT    NOT NULL
            )
        """.trimIndent()

        private val SQL_CREAR_JORNADAS = """
            CREATE TABLE jornadas (
                id               INTEGER PRIMARY KEY AUTOINCREMENT,
                fecha            TEXT    NOT NULL UNIQUE,
                estado           TEXT    NOT NULL DEFAULT 'abierta',
                total_ingresos   REAL    NOT NULL DEFAULT 0,
                total_costos     REAL    NOT NULL DEFAULT 0,
                total_gastos     REAL    NOT NULL DEFAULT 0,
                meta_ganancia    REAL    NOT NULL DEFAULT 0
            )
        """.trimIndent()

        private val SQL_CREAR_VENTAS = """
            CREATE TABLE ventas (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                jornada_id  INTEGER NOT NULL,
                fecha       TEXT    NOT NULL,
                FOREIGN KEY (jornada_id) REFERENCES jornadas(id)
            )
        """.trimIndent()

        private val SQL_CREAR_DETALLE_VENTAS = """
            CREATE TABLE detalle_ventas (
                id              INTEGER PRIMARY KEY AUTOINCREMENT,
                venta_id        INTEGER NOT NULL,
                producto_id     INTEGER NOT NULL,
                cantidad        REAL    NOT NULL,
                precio_unitario REAL    NOT NULL,
                costo_unitario  REAL    NOT NULL,
                FOREIGN KEY (venta_id)    REFERENCES ventas(id),
                FOREIGN KEY (producto_id) REFERENCES productos(id)
            )
        """.trimIndent()

        private val SQL_CREAR_GASTOS = """
            CREATE TABLE gastos_operativos (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                jornada_id  INTEGER NOT NULL,
                descripcion TEXT    NOT NULL,
                monto       REAL    NOT NULL,
                tipo        TEXT    NOT NULL DEFAULT 'variable',
                fecha       TEXT    NOT NULL,
                FOREIGN KEY (jornada_id) REFERENCES jornadas(id)
            )
        """.trimIndent()

        private val SQL_CREAR_MOVIMIENTOS = """
            CREATE TABLE movimientos_inventario (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                producto_id INTEGER NOT NULL,
                tipo        TEXT    NOT NULL,
                cantidad    REAL    NOT NULL,
                fecha       TEXT    NOT NULL,
                observacion TEXT,
                FOREIGN KEY (producto_id) REFERENCES productos(id)
            )
        """.trimIndent()

        private val SQL_CREAR_CONFIGURACION = """
            CREATE TABLE configuracion (
                id                   INTEGER PRIMARY KEY DEFAULT 1,
                nombre_negocio       TEXT    NOT NULL DEFAULT 'Mi Negocio',
                meta_ganancia_diaria REAL    NOT NULL DEFAULT 0
            )
        """.trimIndent()
    }
}
