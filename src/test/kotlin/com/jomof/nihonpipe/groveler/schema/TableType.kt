package com.jomof.nihonpipe.groveler.schema

import org.h2.mvstore.type.DataType
import org.h2.mvstore.type.ObjectDataType
import kotlin.reflect.KClass

data class TableType<T : Any>(
        val clazz: KClass<T>,
        val dataType: DataType = ObjectDataType())