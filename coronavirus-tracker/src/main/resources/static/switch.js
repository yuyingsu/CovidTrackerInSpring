$(document).ready(function() {
    $('#button2').on("click", function () {
        $('#table2').show();
        $('#table1').hide();
    });
    $('#button1').on("click", function () {
        $('#table1').show();
        $('#table2').hide();
     });
     });