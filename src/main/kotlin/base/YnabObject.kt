package base

import budget.YnabTransaction
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue

open class YnabObject {
    var ynabId = ""
    var name = ""

    @JsonIgnore
    val mapper = ObjectMapper().registerModule( KotlinModule() )

    @JsonIgnore
    fun getJson(): String {
        return mapper.writeValueAsString(this )
    }

    @JsonIgnore
    fun createFromJson( jsonValue : String ) : YnabObject {
        return mapper.readValue( jsonValue )
    }

    @JsonIgnore
    protected fun loadYnabId(jsonObject: JsonObject) {
        ynabId = jsonObject.getString( "id" )
    }
}