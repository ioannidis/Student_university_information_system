$(document).ready(function() {
    $('#dataTable-list').DataTable({
    	"iDisplayLength": 25
    });
    $('#dataTable').DataTable({
    	"iDisplayLength": 25
    });
    $('table.table-semester').DataTable({
    	"paging": false
    });
    $('table.table-average').DataTable({
    	"paging": false
    });
    
    $('table.prof-courses').DataTable({
    	"iDisplayLength": 25
    });
} );

