package nodomain.seven.dip.utils

interface CrudDAO<ID, DATA> {
    fun load(identifier: ID): DATA

    fun createIfNotExists(identifier: ID)

    fun save(identifier: ID, toBeSaved: DATA)
}