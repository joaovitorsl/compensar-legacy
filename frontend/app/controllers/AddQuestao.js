angular.module('app')
    .controller('AddQuestao',  function($scope, $location,$http)
    {

        $scope.title = '';
        $scope.changeDetected = false;

        $scope.editorCreated = function (editor) {
            console.log(editor);
        };
        $scope.contentChanged = function (editor, html, text) {
              $scope.changeDetected = true;
            console.log('editor: ', editor, 'html: ', html, 'text:', text);
        };
    



    });