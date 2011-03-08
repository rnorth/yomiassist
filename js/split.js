$(function(){

//    var newContent = document.createElement("div");
//    $("#content").parent().append(newContent);
//    
//    var text = $("#content").text().split(" ");
//    $.each(text, function(i, val){
//        var newSpan = $("<ruby original='"+val+"'>"+val+"<rt>"+val+"</rt></ruby>");
//        $(newContent).append(newSpan);
//        $(newContent).append(document.createTextNode(" "));
//    });
//    
//    $("#content").remove();
    
	// Remove blank ruby elements
	$.each($("ruby"), function(i,char) {
		if ($(char).text() == "") {
			$(char).remove();
		}
	});
	
    var vocabBox;
    var page = 1;
    var pageHeight = 450;
    var lastWordOnPage = null;
	var seenOnThisPage = new Number();
    
    $.each($("ruby"), function(i, val){

        var y = $(val).offset().top;
        var yRelToPage = y - ((page - 1) * pageHeight);
        var newPage = yRelToPage > pageHeight;
        
		// Create a new vocab box if one doesn't exist (i.e. we're on a new page)
		if (vocabBox == null) {
			vocabBox = $(document.createElement("div"));
            vocabBox.addClass("vocab");
            $(val).before(vocabBox);
		}
		
		var thisNodeText = $(val).textChildren(0);
        if (newPage) {
			
			console.log("Breaking: word='"+thisNodeText+"' page="+page+" y="+y+" yRelToPage="+yRelToPage+" newPage="+newPage);
			
            $(val).addClass("break");
            $(val).before("<div class='break'> </div>");
            
            vocabBox = $(document.createElement("div"));
            vocabBox.addClass("vocab");
            $(val).before(vocabBox);
            
            page++;
			seenOnThisPage = new Object();
        }
        else {
            lastWordOnPage = $(val);
            lastWordOnPage.after(vocabBox);
        }
		
		// Keep track of whether a word has been seen before on this page
		if (seenOnThisPage[thisNodeText] == undefined) {
			seenOnThisPage[thisNodeText] = true;
			
			var definition = $(val).attr("definition");
			if (definition != '' && definition != undefined) {
				vocabBox.append("<span>" + definition + "</span> ");
			}
			
		} else {
			$(val).find("rt").remove();
		}
    });
});
