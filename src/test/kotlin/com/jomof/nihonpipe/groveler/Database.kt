package com.jomof.nihonpipe.groveler

import java.io.File

class Database(val root: File) {
    val map: MutableMap<String, Index> = mutableMapOf()

    class WithinKey(val db: Database, key: String, keyType: String) {
        val index = db.getKeyTypeIndex(keyType)
        val alreadyExisted = index.containsKey(key)
        val ordinal = index.getIndex(key)
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

        fun write(value: String, valueType: String): WithinKey {
            if (!db.valueTypeFile(folder, valueType).isFile) {
                db.valueTypeFile(folder, valueType).writeText(value)
            }
            return this
        }
    }

    fun valueTypeFile(folder: File, valueType: String) =
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

    fun save() {
        for ((keyType, index) in map) {
            val file = getKeyTypeIndexFile(keyType)
            if (index.hasChanged()) {
                index.writeFile(file)
            }
        }
    }

    class NodeContext(val db: Database,
                      val keyTypeFolder: File,
                      val index: Index,
                      val ordinal: Int) {
        fun hasValueType(valueType: String): Boolean {
            return getValueTypeFile(valueType).isFile
        }

        fun getValueTypeFile(valueType: String): File {
            return db.valueTypeFile(keyTypeFolder, valueType)
        }

        fun getIndexSize(): Int {
            return index.next
        }
    }

    fun forEach(keyType: String, action: (NodeContext) -> Unit) {
        val index = getKeyTypeIndex(keyType)

        index.map.forEach { _, ordinal ->
            action(NodeContext(this, File(root, "$keyType/$ordinal"), index, ordinal))
        }
    }
}