
    /*
        Gather the product information from the table and submit to server API
        for generating the navigation
     */
    function GetCellValues() {
        const quantity = document.getElementsByName('quantity');
        const table = document.getElementById('itemList');
        let finalValue = "";

        for (let r = 0, n = table.rows.length; r < n; r++) {
            if (r+1 <= n-1) {
                let name = table.rows[r + 1].cells[0].innerText;
                let prodId = table.rows[r + 1].cells[2].innerText;
                const qty = quantity.item(r).value;
                const value = "{\"name\":\"" + name.replace(/[^\w\s]/gi, '') + "\",\"quantity\":" + qty + ",\"prodId\":\" "+prodId+" \"}";

                if (finalValue != "") {
                    finalValue = finalValue + "," +value
                } else {
                    finalValue = value
                }
            }
        }


        post(validate_shopping_note_url, "{\"store_name\":\"" + store.value + "\", \"items\": [" + finalValue + "]}")
            .then(response => response.json())
            .then(data => {
                //console.log(data)
                confirmationTableDiv.hidden = false;
                confirmationTable.innerHTML = "<tr class='active-row'><td>Name</td><td>Quantity</td><td>Action</td></tr>"
                for (let r = 0; r < data["items"].length; r++) {
                    const tr = createNewTr()
                    tr.className="active-row"

                    const td1 = createNewTd()
                    const td2 = createNewTd()
                    const td3 = createNewTd()

                    td1.innerHTML = data["items"][r]["name"]
                    td2.innerHTML = data["items"][r]["quantity"]
                    td3.innerHTML = data["items"][r]["prodId"]

                    tr.appendChild(td1)
                    tr.appendChild(td2)
                    tr.appendChild(td3)
                    confirmationTable.appendChild(tr)
                    confirmationTable.hidden=false
                }
            }).catch(reason => {
            genericError(reason)
        })
    }

    /*
        Auto complete suggestion API call
     */
    function autoComplete(value) {
        suggestionBox.innerHTML = ""

        post(auto_complete_url, "{\"token\":\""+value+"\"}")
            .then(response => response.json())
            .then(data => {
                if (data.length == 0) {
                    suggestedElementSet.clear()
                }

                for (let i = 0; i < data.length; i++) {
                    const product = data[i]
                    suggestedElementSet.set(product["prod_id"], product["product_full_name"])
                }
            }).catch(reason => {
            genericError(reason)
        })

        let count = 0
        suggestedElementSet.forEach((product_name, product_id) => {
            let div = document.createElement("div")
            div.setAttribute('onclick', 'addToShoppingList("'+product_name+'","'+product_id+'")')
            div.setAttribute("id", "suggestion_"+count)
            div.setAttribute("style", suggStyle)
            div.innerHTML = product_name.toString()

            let span = document.createElement("span")
            span.setAttribute("id", "suggestion_span_"+count)
            span.innerHTML = product_id.toString()
            span.setAttribute('hidden', 'true')
            div.appendChild(span)

            suggestionBox.appendChild(div)
            count++
        })
        count = 0
    }


    function actionOnKeyUp(event) {
        const keyCode = event.keyCode == 0 ? event.charCode : event.keyCode;
        const isAlphaNum = ((keyCode >= 48 && keyCode <= 57) || (keyCode >= 65 && keyCode <= 90) || (keyCode >= 97 && keyCode <= 122) || (keyCode == 32));

        if (keyCode == 13) {
            addToShoppingList()
        } else if (keyCode == 8 || keyCode == 46) {
            navCurrentIndex = 0;
            if (element.value.length <= 0) {
                suggestionBox.innerHTML = ""
            }
            if(element.value.length >= 3){
                autoComplete(element.value)
            }
        } else if(keyCode == 40 || keyCode == 38) {

            const numOfSug = suggestionBox.childNodes.length;
            if(navCurrentIndex >= -1) {
                if (keyCode == 38 && navCurrentIndex > 0){
                    navCurrentIndex--
                }

                const sugProdDiv = document.getElementById("suggestion_"+navCurrentIndex)
                if (sugProdDiv != undefined && sugProdDiv != null){
                    sugProdDiv.setAttribute("style", suggStyleSelc)
                    currProdId = document.getElementById("suggestion_span_"+navCurrentIndex).innerText
                    currProdName = sugProdDiv.innerText
                }

                //Reset the other suggestion div color after every new highlight
                suggestionBox.childNodes.forEach((value, key) => {
                    if (navCurrentIndex != key) {
                        value.setAttribute("style", suggStyle)
                    }
                })


                if (keyCode == 40 && navCurrentIndex < numOfSug-1){
                    navCurrentIndex++
                }
            }

        } else if(isAlphaNum){
            if(element.value.length >= 3){
                autoComplete(element.value)
            }
        }
    }


    function addToShoppingList(optionalProdNm, optionalProdId) {
        suggestionBox.innerHTML = ""

        if (formSubmission.hidden) {
            formSubmission.hidden = false;
        }

        if (tableDiv.hidden) {
            tableDiv.hidden = false
        }

        //If suggestion is navigated bu up or down key
        if (currProdName != null && currProdId != null) {
            optionalProdNm = currProdName
            optionalProdId = currProdId
        }

        let newText
        let prodId
        if (optionalProdNm != null) {
            newText = filterSpacial(optionalProdNm)
            prodId = filterSpacial(optionalProdId)
        } else {
            newText = filterSpacial(element.value)
            prodId = filterSpacial(element.value)
        }

        if (newText != '') {
            const tr = document.createElement("tr");
            tr.className="active-row"
            const td1 = document.createElement("td");
            const td2 = document.createElement("td");
            const td3 = document.createElement("td");

            td1.innerHTML = "<label>"+newText+"</label>"
            td2.innerHTML = "<div class=\"quantity buttons_added\">\n" +
                "                <input type=\"button\" value=\"-\" class=\"minus\">\n" +
                "                <input type=\"number\" step=\"1\" min=\"1\" max=\"\" name=\"quantity\" value=\"1\" title=\"Qty\" class=\"input-text qty text\" size=\"4\" pattern=\"\" inputmode=\"\">\n" +
                "                <input type=\"button\" value=\"+\" class=\"plus\">\n" +
                "            </div>"
            td3.innerHTML = prodId
            td3.hidden = true

            tr.appendChild(td1)
            tr.appendChild(td2)
            tr.appendChild(td3)
            table.appendChild(tr)
        }

        document.getElementById("itemText").value = ""
        navCurrentIndex = -1
        element.focus()
        currProdName = null
        currProdId = null
    }

    function changeStore() {

        if (store.value != "select") {
            resetBtn.hidden = false
            store.disabled=true
            itemInput.hidden = false;
            element.focus()
        }

        navCurrentIndex = -1
        currProdName = null
        currProdId = null
    }

    function reset() {
        formSubmission.hidden = true;
        table.innerHTML = '';

        if(document.getElementById("tableHeader") == null) {
            const trHeader = document.createElement("tr");
            trHeader.innerHTML = "<tr id='tableHeader'><td>Name</td><td>Quantity</td><td></td></tr>"
            table.appendChild(trHeader)
            tableDiv.hidden = true
            table.className = "styled-table"
        }
        resetBtn.hidden = true
        store.disabled=false
        itemInput.hidden = true;
        navCurrentIndex = -1

        currProdName = null
        currProdId = null
    }

