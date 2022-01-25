
    const host = "http://localhost"
    const port = "8080"
    const validate_shopping_note_url = host+":"+port+"/v1/validate/";
    const auto_complete_url = host+":"+port+"/v1/search/autocomplete/";
    const match_tag_url = host+":"+port+"/v1/search/matchTags/";
    const find_store_url = host+":"+port+"/v1/search/store/";

    let suggestedElementSet = new Map()
    let navCurrentIndex = -1;
    let maxSugProd = 10;

    const suggStyle = "border: 1px solid #aaaaaa; border-top: 0px; padding-top:10px; font-weight: regular; background-color: #ececf6; width: 70%"
    const suggStyleSelc = "border: 1px solid #aaaaaa; border-top: 0px; padding-top:12px; font-weight: bold; background-color: #6c757d; width: 70%"

    const storeLocSuggStyle = "border: 1px solid #aaaaaa; border-top: 0px; padding-top:2px; font-weight: regular; background-color: #ececf6; width: 70%"
    const storeLocSuggStyleSelc = "border: 1px solid #aaaaaa; border-top: 0px; padding-top:2px; font-weight: bold; background-color: #6c757d; width: 70%"

    const suggStyleMouseOver = "border: 1px solid #aaaaaa; border-top: 0px; padding-top:10px; font-weight: bold"
    const suggStyleMouseLeave = "border: 1px solid #aaaaaa; border-top: 0px; padding-top:10px; font-weight: regular"

    let orderId = null
    let store
    let resetBtn
    let element
    let tableDiv
    let table
    let itemInput
    let formSubmission
    let confirmationTable
    let suggestionBox
    let currProdId = null
    let currProdName = null
    let reviewItemsText = null
    let loadingAnimation = null
    let backToProdList = null
    let selectStoreLabel = null

    function init() {
        document.getElementById("sendToEmail").hidden = true
        selectStoreLabel = document.getElementById("selectStoreLabel")

        reviewItemsText = document.getElementById("reviewItemsText")
        store = document.getElementById("store");
        resetBtn = document.getElementById("reset");
        element = document.getElementById("itemText")
        tableDiv = document.getElementById("tableDiv")
        table = document.getElementById("itemList")
        itemInput = document.getElementById("itemInput")
        formSubmission = document.getElementById("formSubmission")
        confirmationTable = document.getElementById("confirmationTable")
        suggestionBox = document.getElementById("suggestionHolder")
        backToProdList = document.getElementById("backToProdList")
        loadingAnimation = $('loadingAnimation')

        tableDiv.hidden = true;
        resetBtn.hidden = true;
        store.disabled = false;
        itemInput.hidden = true;
        formSubmission.hidden = true
        reviewItemsText.hidden = true;
        suggestionBox.innerHTML = "";
        loadingAnimation.hidden = true
        backToProdList.hidden = true;
        selectStoreLabel.hidden = true;
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

    function get(url) {
        console.log("GET: "+url)
        return fetch(url, {
            method: 'GET',
            headers: {'Accept': 'application/json', 'Content-Type': 'application/json'}
        })
    }

    function autoCompleteOnMouseOverORLeave(obj, flag) {

        if(flag = "over") {//console.log(obj)
            //obj.setAttribute("style", suggStyleSelc)
        } else if(flag = "leave") {//console.log(obj)
            //obj.setAttribute("style", suggStyle)
        } else {
            console.log(obj)
        }
    }


    const loader = document.querySelector(".loader");
    window.onload = function(){
        setTimeout(function(){
            loader.style.opacity = "0";
            setTimeout(function(){
                loader.style.display = "none";
            }, 500);
        },1500);
    }

    function capFirstLetter(string) {
        if (string != null && string.length > 1) {
            return string.charAt(0).toUpperCase() + string.slice(1);
        } else return string

    }

    function generateUUID() {
        let d = new Date().getTime();//Timestamp
        let d2 = ((typeof performance !== 'undefined') && performance.now && (performance.now() * 1000)) || 0;//Time in microseconds since page-load or 0 if unsupported
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            let r = Math.random() * 16;//random number between 0 and 16
            if(d > 0){//Use timestamp until depleted
                r = (d + r)%16 | 0;
                d = Math.floor(d/16);
            } else {//Use microseconds since page-load if supported
                r = (d2 + r)%16 | 0;
                d2 = Math.floor(d2/16);
            }
            return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
        });
    }