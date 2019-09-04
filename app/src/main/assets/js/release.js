var signing = document.querySelectorAll(".wpforms-signature-wrap");
Android.showToast(signing.length);

console.log(signing);

for(var i = 0; i < signing.length; i++) {
    var node = signing[i];

    var button = document.createElement("button");
    button.href = '#';
    button.setAttribute('style', 'color: black; border-style: solid; border-width: 4px; padding: 8px; border-radius: 4px');
    button.innerHTML = 'Tap to sign';
    button.addEventListener("click", function(event) {
        event.preventDefault();
        Android.showToast("Clicked!");
    });

    console.log(node);
    console.log(node.parentNode);

    node.parentNode.replaceChild(button, node);
}