/* global host */
angular.module('app')
  .factory('QuestoesService', function($rootScope,$http,$q,Notification,$location, AuthService, $sce) {
   const service = {};
   let deferred = $q.defer();

   service.getQuestoes = function (pageNumber, usersPerPage) {
      $http.get(host + 'questao/' + pageNumber + '/' + usersPerPage, AuthService.getAuthorization()).
      then(function (response) {
        $rootScope.Questoes = response.data.content;
        $rootScope.totalQuestoes = response.data.totalElements;
        $rootScope.pageNumber = response.data.number;
        $rootScope.totalPags = response.data.totalPages;
        $rootScope.loading = false;

        
        deferred.resolve(response.data);
      }, function (err) {
        if (err.status == 400) {
          $rootScope.forceSignOut();
        }
        deferred.resolve([]);
        $rootScope.loading = false;
      });

    return deferred.promise;
   },


   service.sendQuery = function (query, pageNumber, usersPerPage, apenasAutor) {
    let estados =  query.estados.length === 0 ? ' ' : query.estados.join(",");
    let competencias =  query.competencias.length === 0 ? ' ' : query.competencias.join(",");
    $http.get(host + 'questao/' + ((apenasAutor) ? 'searchMy/' : 'search/') + query.enunciado + '/' + competencias + '/' + ((apenasAutor) ? estados + '/' : '')
    + ((apenasAutor) ? '' : query.autor + '/') + query.fonte + '/' + query.tipo + '/' + query.conteudo + '/' + pageNumber + '/' + usersPerPage, AuthService.getAuthorization()).
      then(function (response) {
        $rootScope.Questoes = response.data.content;
        $rootScope.totalQuestoes = response.data.totalElements;
        $rootScope.pageNumber = response.data.number;
        $rootScope.totalPags = response.data.totalPages;
        $rootScope.loading = false;


        if (query.enunciado !== "") $rootScope.adicionaMarcadores(query.enunciado);


        deferred.resolve(response.data);
     
      }, function (err) {
        if (err.status == 400) {
          $rootScope.forceSignOut();
        }
        deferred.resolve([]);

        

      });

    return deferred.promise;
   },


  
  service.removeQuestao = function (questao) {
        $http.delete(host + 'questao/' + questao.id, AuthService.getAuthorization()).
          then(function (response) {
            var  index = $rootScope.Questoes.indexOf(questao);
            $rootScope.Questoes.splice(index,1);
            hideModals();
            $rootScope.loading = false;
            Notification.success('Questão removida com sucesso!');
            $location.path("/questoes");
          }).catch(function (err) {
            if (err.status == 400) {
              $rootScope.forceSignOut();
            } else {
              Notification.error('Falha ao remover questão!');
            }
            deferred.resolve([]);
          });
  },


  service.atualizaQuestao = function (questao,novaQuestao) {
    $rootScope.loading = true;

    $http.put(host + 'questao/' + questao.id, novaQuestao, AuthService.getAuthorization()).
      then(function (response) {
        let index = 0;
        for(; index < $rootScope.Questoes.length; index++) {
          if ($rootScope.Questoes[index].id === questao.id) {
            break;
          }
        }
        $rootScope.Questoes.splice(index,1,response.data);
        $rootScope.loading = false;
        hideModals();

        Notification.success('Questão atualizada com sucesso!');
        $location.path("/questoes");
    },function(err){
      if (err.status == 400) {
        $rootScope.forceSignOut();
      } else {
        Notification.error('Falha ao atualizar questão!');
        $location.path("/questoes");
      }
      $rootScope.loading = false;
    });
},

service.publicaQuestao = function (questao) {
  $rootScope.loading = true;

  $http.put(host + 'questao/publish/' + questao.id, {}, AuthService.getAuthorization()).
    then(function (response) {
      var  index = $rootScope.Questoes.indexOf(questao);
      $rootScope.Questoes.splice(index,1,response.data);
      $rootScope.loading = false;
      hideModals();

      Notification.success('Questão publicada com sucesso!');
      $location.path("/questoes");
  },function(err){
    if (err.status == 400) {
      $rootScope.forceSignOut();
    } else {
      Notification.error('Falha ao publicar questão!');
          $location.path("/questoes");
    }
  });
},

  service.sendListaQuestao = function (lista) {
      let questoes = [];
      for(let questao of lista.questoes) {
        questoes.push(questao.id);
      }
      lista.questoes = questoes;
      $http.post(host + 'listaquestoes', lista, AuthService.getAuthorization()).
        then(function (response) {
          Notification.success('Lista criada com sucesso!');
          $location.path("/questoes");
          service.getListaQuestoes();
            
        },function(err){
          if (err.status == 400) {
            $rootScope.forceSignOut();
          } else {
            Notification.error('Falha no envio da lista!');
            $location.path("/questoes");
          }
        }
    )
  },

  service.getListaQuestoes = function () {
    $http.get(host + 'listaquestoes/0/100', AuthService.getAuthorization()).
      then(function (response) {
        $rootScope.listas = response.data.content;
        deferred.resolve(response.data.content);
      }, function (err) {
        if (err.status == 400) {
          $rootScope.forceSignOut();
        }
        deferred.resolve([]);
      });

  return deferred.promise;
 },

 service.getListaQuestoesById = function (lista) {
  $http.get(host + 'listaquestoes/' + lista.id, AuthService.getAuthorization()).
  then(function (response) {
    $rootScope.Questoes = response.data.questoes;
    deferred.resolve(response.data);
  }, function (err) {
    if (err.status == 400) {
      $rootScope.forceSignOut();
    }
    deferred.resolve([]);
  });

  return deferred.promise;
 },

   
 service.removeLista = function (lista) {

  $http.delete(host + 'listaquestoes/' + lista.id, AuthService.getAuthorization()).
    then(function (response) {
      $location.path("/questoes");
      Notification.success('Lista removida com sucesso!');
    }).catch(function (err) {
      if (err.status == 400) {
        $rootScope.forceSignOut();
      } else {
        Notification.error('Falha ao remover lista!');
        $location.path("/questoes");      
      }
      deferred.resolve([]);
    });
},

service.sendUpdateLista = function (lista,novaLista) {
  let questoes = [];
  for(let questao of novaLista.questoes) {
    questoes.push(questao.id);
  }
  novaLista.questoes = questoes;
  $http.put(host + 'listaquestoes/' + lista.id, novaLista, AuthService.getAuthorization()).
    then(function (response) {
    let index = 0;
    for(; index < $rootScope.listas.length; index++) {
      if ($rootScope.listas[index].id === lista.id) {
        break;
      }
    }
    $rootScope.listas[index] = response.data;
    $rootScope.listaEmExibicao = response.data;

    Notification.success('Lista atualizada com sucesso!');
    return response.data;
  },function(err){
    if (err.status == 400) {
      $rootScope.forceSignOut();
    } else {
      Notification.error('Falha ao atualizar lista!');
        $location.path("/questoes");
    }
  });
},

service.getCompetencias = function (enunciado) {
  return $http.post(host + 'competencias', enunciado, AuthService.getAuthorization()).
    then(function (response) {
      $rootScope.competencias = response.data;
      $rootScope.loading = false;
      console.log($rootScope.competencias)
      Notification.success('Feedback das competências obtido com sucesso!');
      return response;
    },function(err) {
      if (err.status == 400) {
        $rootScope.forceSignOut();
      } else {
        Notification.error('Falha no feedback das competências!');
        $rootScope.loading = false;
      }
      return err;
    }
  )
}

service.getQuestaoPendente = function() {
  return $http.get(host + 'questao/pendente/', AuthService.getAuthorization())
  .then(function(response) {
    $rootScope.questaoSobAvaliacao = response.data;
    $rootScope.questaoSobAvaliacao.enunciado = $sce.trustAsHtml($rootScope.questaoSobAvaliacao.enunciado);
  }, function(err) {
    if (err.status == 400) {
      $rootScope.forceSignOut();
    } else {
      Notification.warning('Nenhuma questão pendente de sua avaliação!');
    }
  })
}

service.getQuestaoAvaliada = function() {
  return $http.get(host + 'questao/avaliada/', AuthService.getAuthorization())
  .then(function(response) {
    return response;
  }, function(err) {
    if (err.status == 400) {
      $rootScope.forceSignOut();
    } else {
      Notification.warning('Nenhuma questão pendente de sua aprovação!');
    }
  })
}

service.aprovaQuestao = function(questao, novaQuestao) {
  return $http.put(host + 'questao/aprove/' + questao.id, novaQuestao, AuthService.getAuthorization())
  .then(function(response) {
    Notification.success('Questão aprovada com sucesso!');
    $rootScope.loading = false;
    hideModals();
    return response;
  }, function(err) {
    if (err.status == 400) {
      $rootScope.forceSignOut();
    } else {
      Notification.error('Falha ao aprovar da questão!');
      $location.path("/questoes");
      $rootScope.loading = false;
      hideModals();
    }
    return err;
  })
}

service.rejeitaQuestao = function(questao) {
  return $http.put(host + 'questao/reject/' + questao.id, {}, AuthService.getAuthorization())
  .then(function(response) {
    let index = -1;
    for(let i = 0; i < $rootScope.Questoes.length; i++) {
      if ($rootScope.Questoes[i].id === questao.id) {
        index = i;
        break;
      }
    }
    if (index !== -1) {
      $rootScope.Questoes.splice(index,1,response.data);
    }
    $rootScope.loading = false;
    Notification.success('Questão rejeitada com sucesso!');
    $location.path("/questoes");
    $rootScope.loading = false;
    hideModals();
  }, function(err) {
    if (err.status == 400) {
      $rootScope.forceSignOut();
    } else {
      Notification.error('Falha ao rejeitar a questão!');
      $location.path("/questoes");
      $rootScope.loading = false;
      hideModals();
    }
  })
}

    return service;
  });