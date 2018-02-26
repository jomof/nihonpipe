package com.jomof.nihonpipe.groveler

import java.io.File

class Database(val root: File) {
    val map: MutableMap<String, Index> = mutableMapOf()
    val originalFilters: MutableMap<String, BitField> = mutableMapOf()
    val filters: MutableMap<String, BitField> = mutableMapOf()

    class WithinKey(val db: Database, key: String, val keyType: String) {
        val index = db.getKeyTypeIndex(keyType)
        val alreadyExisted = index.containsKey(key)
        val ordinal = index.getOrdinal(key)
        val folder = File(db.root, "$keyType/$ordinal")

        init {
            if (!alreadyExisted) {
                if (folder.isDirectory) {
                    // Ordinal folder existed but wasn't in index.
                    // It's garbage, collect it.
                    folder.deleteRecursively()
                }
            }
            if (folder.mkdirs()) {
                File(folder, "key.txt").writeText(key)
                File(folder, "key-type.txt").writeText(keyType)
            }
        }

        fun readBitField(valueType: String): BitField {
            val key = getKeyTypeValueTypeKey(valueType)
            if (db.filters.containsKey(key)) {
                return db.filters[key]!!
            }
            val file = File(db.root, "$key.index.txt")
            val filter =
                    if (file.exists()) {
                        val init = file.readLines()[0]
                        BitField(init)
                    } else {
                        createBitField()
                    }
            db.originalFilters[key] = filter
            db.filters[key] = filter
            return filter
        }

        fun getAndSetFilter(valueType: String): Boolean {
            val filter = readBitField(valueType)
            return if (filter[ordinal]) {
                true
            } else {
                val key = getKeyTypeValueTypeKey(valueType)
                db.filters[key] = filter.set(ordinal, true)
                false
            }
        }

        private fun getKeyTypeValueTypeKey(valueType: String) = "$keyType~$valueType"

        fun write(value: String, valueType: String): WithinKey {
            if (!getAndSetFilter(valueType)) {
                db.getValueTypeFile(folder, valueType).writeText(value)
            }
            return this
        }

        fun overwrite(value: String, valueType: String): WithinKey {
            getAndSetFilter(valueType)
            db.getValueTypeFile(folder, valueType).writeText(value)
            return this
        }
    }

    fun getValueTypeFile(folder: File, valueType: String) =
            File(folder, "$valueType.txt")

    fun withinKey(key: String, keyType: String): WithinKey {
        return WithinKey(this, key, keyType)
    }

    private fun getKeyTypeIndex(keyType: String): Index {
        if (map.containsKey(keyType)) {
            return map[keyType]!!
        }
        val typeIndexFile = getKeyTypeIndexFile(keyType)
        map[keyType] = readIndex(typeIndexFile)
        return getKeyTypeIndex(keyType)
    }

    private fun getKeyTypeIndexFile(keyType: String): File {
        return File(root, "$keyType.txt")
    }

    fun save(): Boolean {
        var savedAny = true
        for ((keyType, index) in map) {
            val file = getKeyTypeIndexFile(keyType)
            if (index.hasChanged()) {
                index.writeFile(file)
            } else {
                savedAny = false
            }
        }

        for ((indexKey, bitfield) in filters) {
            val originalFilter = originalFilters[indexKey] ?: createBitField()
            if (originalFilter.init != bitfield.init) {
                val file = File(root, "$indexKey.index.txt")
                file.writeText(bitfield.init)
                savedAny = true
            }
        }
        return savedAny
    }

    class NodeContext(val db: Database,
                      val keyTypeFolder: File,
                      val index: Index,
                      val ordinal: Int) {
        fun hasValueType(valueType: String): Boolean {
            return getValueTypeFile(valueType).isFile
        }

        fun getValueTypeFile(valueType: String): File {
            return db.getValueTypeFile(keyTypeFolder, valueType)
        }

        fun getIndexSize(): Int {
            return index.next
        }
    }

    fun forEach(keyType: String, action: (NodeContext) -> Unit) {
        val index = getKeyTypeIndex(keyType)

        index.map.forEach { _, ordinal ->
            action(NodeContext(this, getKeyTypeOrdinalFolder(keyType, ordinal), index, ordinal))
        }
    }

    private fun getKeyTypeOrdinalFolder(keyType: String, ordinal: Int) = File(root, "$keyType/$ordinal")

    fun get(key: String, keyType: String, valueType: String): String? {
        val index = getKeyTypeIndex(keyType)
        val ordinal = index.getOrdinal(key)
        val folder = getKeyTypeOrdinalFolder(keyType, ordinal)
        val file = getValueTypeFile(folder, valueType)
        if (!file.exists()) {
            return null
        }
        return file.readText()
    }
}