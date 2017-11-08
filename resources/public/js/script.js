$(document).ready(function() {
    $('#results-table').DataTable({
        columnDefs: [
            {
                render: function(data) {
                    if (data == "") {
                        return "Price not available";
                    } else {
                        return data;
                    }
                },
                targets: 0
            }
        ]
    });
});
