var signing = document.querySelectorAll(".wpforms-signature-wrap");

for(var i = 0; i < signing.length; i++) {
    var node = signing[i];

    var button = document.createElement("button");
    button.href = '#';
    button.setAttribute('style', 'color: black; border-style: solid; border-width: 4px; padding: 8px; border-radius: 4px');
    button.innerHTML = 'Tap to sign';
    button.addEventListener("click", function(event) {
        event.preventDefault();
        var textNode = event.target.parentNode.querySelectorAll(".wpforms-signature-input")[0];
        var textNodeName = textNode.getAttribute("Name")
        Android.showSigningForm(textNodeName);
    });

    node.parentNode.replaceChild(button, node);
}

Android.setupFinished();