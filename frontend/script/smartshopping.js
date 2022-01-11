
    /*
        Gather the product information from the table and submit to server API
        for generating the navigation
     */
    function getCellValues() {
        const quantity = document.getElementsByName('quantity');
        const table = document.getElementById('itemList');
        let finalValue = "";

        for(let i = 1; i<table.rows.length; i++) {
            let row = table.rows[i]
            let prod_id = row.cells[0].innerText
            if (prod_id == "--") {
                alert("You have not selected the product.")
                let sect = row.cells[1].childNodes[0]
                sect.focus()
                sect.setAttribute("style", sect.getAttribute("style")+"; color:red")
                return;
            }
            let prod_full_name = row.cells[1].innerText
            let qty = quantity.item(i-1) == null ? 0: quantity.item(i-1).value;
            const value = "{\"name\":\"" + prod_full_name.replace(/[^\w\s]/gi, '') + "\",\"quantity\":" + qty + ",\"prodId\":\""+prod_id+"\"}";

            if (finalValue != "") {
                finalValue = finalValue + "," +value
            } else {
                finalValue = value
            }
        }

        let reqJson = "{\"store_name\":\"" + store.value + "\", \"items\": [" + finalValue +"]}";
        console.log(reqJson)

        post(validate_shopping_note_url, reqJson)
            .then(response => response.json())
            .then(data => {
                console.log(data)
            }).catch(reason => {
            genericError(reason)
        })
    }

    /*
        Auto complete suggestion API call
     */
    function autoComplete(value) {
        suggestionBox.innerHTML = ""
        post(auto_complete_url, "{\"token\":\""+value+"\",\"store_name\":\""+store.value+"\"}")
            .then(response => response.json())
            .then(data => {
                if (data.length == 0) {
                    suggestedElementSet.clear()
                }

                for (let i = 0; i < data.length; i++) {
                    const product = data[i]
                    suggestedElementSet.set(product["prod_id"], product["product_full_name"])
                }

                let count = 0
                suggestedElementSet.forEach((product_name, product_id) => {
                    let div = document.createElement("div")
                    div.setAttribute('onclick', 'addToShoppingList("'+product_name+'","'+product_id+'")')
                    div.setAttribute('onmouseover', 'autoCompleteOnMouseOverORLeave(this, \'over\')')
                    div.setAttribute('onmouseleave', 'autoCompleteOnMouseOverORLeave(this, \'leave\')')
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
            }).catch(reason => {
            genericError(reason)
        })
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
                suggestedElementSet.clear()
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
        reviewItemsText.hidden = false;

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
            populateProductTable(newText, prodId, null)
        } else {
            newText = filterSpacial(element.value)
            prodId = null

            post(match_tag_url, "{\"token\":\""+newText+"\",\"store_name\":\""+store.value+"\"}")
                .then(response => response.json())
                .then(data => {
                    console.log(data)
                    populateProductTable(newText, prodId, data)
                }).catch(reason => {
                genericError(reason)
            })
        }
    }

    function changeStore(inputValue, fromLink) {

        if (inputValue != null || store.value != "select") {
            resetBtn.hidden = false
            store.disabled=true
            itemInput.hidden = false;
            element.focus()
        }

        if (inputValue != null) {
            itemInput.value = inputValue
        }
        navCurrentIndex = -1
        currProdName = null
        currProdId = null
        suggestedElementSet.clear()

        if (fromLink != null) {
            store.selectedIndex = fromLink
        }
    }

    function reset() {
        formSubmission.hidden = true;
        reviewItemsText.hidden = true;
        table.innerHTML = '';

        if(document.getElementById("tableHeader") == null) {
            const trHeader = document.createElement("tr");
            trHeader.setAttribute("style", "font-weight: bold")
            trHeader.setAttribute("id", "tableHeader")
            trHeader.innerHTML = "<tr>" +
                "<td>SKU</td>" +
                "<td>Product name</td>" +
                "<td>Quantity</td>" +
                "</tr>"
            table.appendChild(trHeader)
            tableDiv.hidden = true
        }
        resetBtn.hidden = true
        store.disabled=false
        store.selectedIndex = 0
        itemInput.hidden = true;
        itemInput.value = ""
        navCurrentIndex = -1

        currProdName = null
        currProdId = null
        suggestedElementSet.clear()
        suggestionBox.innerHTML = ""

    }

    function populateProductTable(newText, prodId, additionalProds) {
        if (newText != '') {
            let quantityCounter = 0;
            const tr = document.createElement("tr");
            const td1 = document.createElement("td");
            const td2 = document.createElement("td");
            const td3 = document.createElement("td");

            if (additionalProds != null && additionalProds.length > 0) {
                const htmlSelectElement = document.createElement("select");
                htmlSelectElement.setAttribute("style", "width:100%; height:20px");
                htmlSelectElement.setAttribute("onchange",'selFromAdditionalProdList(this)')

                const htmlOptionElement = document.createElement("option");
                htmlOptionElement.innerHTML = "<==== Select your product matching with '"+newText+"' ====>";
                htmlSelectElement.appendChild(htmlOptionElement)

                for (let i = 0; i < additionalProds.length; i++) {
                    let prd_id = additionalProds[i]["prod_id"]
                    let prd_full_nm = additionalProds[i]["product_full_name"]

                    const newOption = document.createElement("option");
                    newOption.setAttribute("id", prd_id)
                    newOption.setAttribute("value", prd_id+"_"+prd_full_nm)
                    newOption.innerHTML = prd_full_nm;
                    htmlSelectElement.appendChild(newOption)
                }
                prodId = "<label>--</label>"
                td1.innerHTML = prodId
                td2.appendChild(htmlSelectElement)
            } else if (prodId == null){
                suggestionBox.innerHTML = "<label style='font-weight: bold; font-size: 10px; color: #c82333'>No product found with '"+newText+"'</label>"
            } else {
                td1.innerHTML = "<label>"+prodId+"</label>"
                td2.innerHTML = newText
                quantityCounter = 1
            }

            if (prodId != null) {
                td3.hidden = false
                td3.innerHTML = "<div class='number'>" +
                    "<span onclick='setQuantity(this)' class='minus' onMouseOver=\"this.style.cursor='pointer'\">-</span>" +
                    "<input name='quantity' class='counterIp' type='text' value="+quantityCounter+" readonly>" +
                    "<span onclick='setQuantity(this)'  class='plus' onMouseOver=\"this.style.cursor='pointer'\">+</span>" +
                    "</div>"

                appendOrPrepend(table, [td1, td2, td3])
            }
        }

        document.getElementById("itemText").value = ""
        navCurrentIndex = -1
        element.focus()
        currProdName = null
        currProdId = null
        suggestedElementSet.clear()
    }

    function selFromAdditionalProdList(obj) {
        let tr = obj.parentNode.parentElement
        if (tr != null && tr.childNodes.length == 3) {
            tr.childNodes[0].innerHTML = obj.value.split("_")[0]
            tr.childNodes[1].innerHTML = obj.value.split("_")[1]

            const quantity = document.getElementsByName('quantity');
            if(quantity.item(tr.rowIndex-1) != null) {
                quantity.item(tr.rowIndex-1).value = 1
            }
        }
    }

    function appendOrPrepend(table, columns) {
        let row = table.insertRow(1)
        columns.forEach(column => {
            row.appendChild(column)
        })
    }

    function setQuantity(obj) {
        let btnVal = obj.innerHTML
        let counterTxtVal = obj.parentNode.childNodes[1].value

        if (btnVal == "-") {
            if (counterTxtVal == 1 || counterTxtVal == 0) {
                let tbl = obj.parentNode.parentNode.parentNode.parentNode
                let currRow = obj.parentNode.parentNode.parentNode
                tbl.removeChild(currRow)
            } else {
                obj.parentNode.childNodes[1].value = new Number(counterTxtVal)-1
            }
        } else if (btnVal == "+") {
            obj.parentNode.childNodes[1].value = new Number(counterTxtVal)+1
        }
    }