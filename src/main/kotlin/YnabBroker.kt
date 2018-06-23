import base.JsonObject
import base.YnabResponse
import budget.*
import budget.category.YnabBudgetCategory
import budget.category.YnabCategoryHistory
import khttp.get
import org.json.JSONArray
import org.json.JSONObject

class YnabBroker(var configuration: YnabConfiguration ) {
    fun getBudgetSummaries() : MutableList<YnabBudgetSummary> {
        val result : MutableList<YnabBudgetSummary> = mutableListOf()

        val dataList = getDataFromYnab( "budgets" )

        dataList.forEach{ result.add( YnabBudgetSummary( it ) ) }

        return result
    }

    fun getBudget( ynabId: String ): YnabBudget {
        val responseData : JsonObject? = getFromYnab("budgets/" + ynabId ).data

        if ( responseData == null ) {
            throw Exception( "Can't find data for budget" )
        }

        val result = responseData.getObject( "budget" )

        return YnabBudget( result )
    }

    fun getOverBudgetCategories( budgetYnabId: String): List<YnabBudgetCategory> {
        val budget = getBudget( budgetYnabId )

        // TODO: Make this real, not just a hardcoded month
        return budget.budgetMonths[1].categories.filter { category -> category.isOverBudget() }
    }

    fun getCategoryHistory(budgetYnabId: String, categoryYnabId: String): YnabCategoryHistory {
        val categoryHistory = YnabCategoryHistory()

        val budget = getBudget( budgetYnabId )

        for ( category in budget.getCategoriesForAllMonths() ) {
            if ( category.ynabId == categoryYnabId ) {
                if ( categoryHistory.name == "" ) {
                    categoryHistory.initialize( category )
                }

                categoryHistory.addItem( category.referenceBudgetMonth, category )
            }

        }

        return categoryHistory
    }

    fun getTransactionsByMemo(budgetYnabId: String, memoText: String): List<YnabTransaction> {
        val matchingTransactions : MutableList<YnabTransaction> = mutableListOf()

        val budget = getBudget( budgetYnabId )

        for( currentTransaction in budget.transactions ) {
            if ( currentTransaction.memoContains( memoText ) ) {
                matchingTransactions.add( currentTransaction )
            }
        }

        return matchingTransactions
    }

    private fun getDataFromYnab( endpointName: String ): List<JsonObject> {
        val responseData = getFromYnab( endpointName ).data

        if ( responseData == null ) {
            throw Exception( "Can't find data for " + endpointName )
        }

        return responseData.getArray( endpointName )
    }

    private fun getFromYnab( endpointName: String ): YnabResponse {
        val response = YnabResponse(get(url = configuration.getUrl(endpointName)))

        if ( response.hasError() ) {
            print( response.errors[0].toString() )
            throw Exception( "Error connecting to YNAB "+ endpointName + " endpoint [" + response.errors[0].toString() + "]" )
        }

        return response
    }
}