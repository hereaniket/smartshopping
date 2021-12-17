
    const host = "http://localhost"
    const port = "8080"
    const validate_shopping_note_url = host+":"+port+"/v1/validate/";
    const auto_complete_url = host+":"+port+"/v1/search/autocomplete/";
    let suggestedElementSet = new Map()
    let navCurrentIndex = -1;
    let maxSugProd = 10;
    const suggStyle = "border: 1px solid #aaaaaa; border-top: 0px; padding-top:10px; background-color: #ececf6;"
    const suggStyleSelc = "border: 1px solid #aaaaaa; border-top: 0px; padding-top:10px; background-color: #6c757d;"

    let store
    let resetBtn
    let element
    let tableDiv
    let table
    let itemInput
    let formSubmission
    let confirmationTableDiv
    let confirmationTable
    let suggestionBox
    let currProdId = null
    let currProdName = null

    function init() {
        store = document.getElementById("store");
        resetBtn = document.getElementById("reset");
        element = document.getElementById("itemText")
        tableDiv = document.getElementById("tableDiv")
        table = document.getElementById("itemList")
        itemInput = document.getElementById("itemInput")
        formSubmission = document.getElementById("formSubmission")
        confirmationTableDiv = document.getElementById("confirmationTableDiv")
        confirmationTable = document.getElementById("confirmationTable")
        suggestionBox = document.getElementById("suggestionHolder")

        tableDiv.hidden = true;
        resetBtn.hidden = true;
        store.disabled = false;
        itemInput.hidden = true;
        formSubmission.hidden = true;
        confirmationTable.hidden = true
    }


    function genericError(reason) {
        if(reason == "TypeError: Failed to fetch"){
            alert("8991: Smart shopping service is down")
        } else {
            alert("8999: Internal System Error")
        }
    }

    function filterSpacial(input) {
        return input.replace(/[`~!@#$%^&*()_|+\-=?;:'",.<>\{\}\[\]\\\/]/gi, '');
    }

    function createNewTd() {
        return document.createElement("td");
    }

    function createNewTr() {
        return document.createElement("tr");
    }

    function post(url, json) {
        console.log("POST: "+url)
        return fetch(url, {
            method: 'POST',
            body: json,
            headers: {'Accept': 'application/json', 'Content-Type': 'application/json'}
        })
    }
