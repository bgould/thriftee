<!doctype html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>ThriftEE Javascript Examples Page</title>
<script type="text/javascript" src="${pageContext.request.contextPath}/jquery-1.11.1.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/clients/jquery/thrift.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/clients/jquery/org_thriftee_examples_presidents_types.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/clients/jquery/PresidentService.js"></script>
<script type="text/javascript">
(function ($) {
    var _mp = new Thrift.Multiplexer(),
        _transport = new Thrift.Transport('${pageContext.request.contextPath}/services/endpoint'),
        _client = _mp.createClient('PresidentService', PresidentServiceClient, _transport);
    ;
    window.client = _client;
    function _getEditorText() {
        return $('#editor-textarea').val();
    }
    $(function () {
        $('#runIt').click(function () {
            var script = _getEditorText(),
                result = null;
            if (console && console.log) {
                console.log('running script\n', script);
            }
            $('#result-script-area').text(script);
            result = eval(script);
            if (console && console.log) {
                console.log('result: ', result);
            }
            if (result) {
                $('#result-value-area').text(JSON.stringify(result));
            } else {
                $('#result-value-area').text('');
            }
        });
    });
}(jQuery));
</script>
<style type="text/css">
.editor-area, .result-area { width: 50%; margin: auto; float: left; }
.result-area pre { overflow: scroll; white-space: pre-wrap; height: 250px; border: 1px solid black; width: 95%; }
</style>
</head>
<body>
    <h1>ThriftEE Javascript Examples Page</h1>
    <div class="editor-area">
        <p><textarea rows="24" cols="80" id="editor-textarea"></textarea></p>
        <p><input type="button" id="runIt" value="Run It!" /></p>
    </div>
    <div class="result-area">
        <pre id="result-script-area"></pre>
        <pre id="result-value-area"></pre>
    </div>
</body>
</html>
