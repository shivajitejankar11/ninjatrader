requirejs.config({
    baseUrl: 'js/lib',
    paths: {
        app: '../app',
        jquery: 'jquery.min',
        jquerySerializeJson: 'jquery.serializejson.min',
        ace: 'ace/ace',
        d3: 'd3.min'
    },

    shim: {
        ace: {
            exports: 'ace'
        }
    }
});


requirejs(['jquery'], function($) {

    /**
     * Similar to request.getParameter("name")
     * @param name
     * @returns parameter value
     */
    $.queryParam = function(name){
        var results = new RegExp('[\?&]' + name + '=([^&#]*)').exec(window.location.href);
        if (results==null){
            return null;
        }
        else{
            return results[1] || 0;
        }
    };
});
