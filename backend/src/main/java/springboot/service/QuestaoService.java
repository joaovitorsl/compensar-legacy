package springboot.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import com.mongodb.BasicDBObject;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.stereotype.Service;

import net.htmlparser.jericho.Source;
import springboot.enums.CompetenciaType;
import springboot.enums.EstadoQuestao;
import springboot.exception.data.NoPendentQuestionException;
import springboot.exception.data.PermissionDeniedException;
import springboot.exception.data.RegisterNotFoundException;
import springboot.model.Questao;
import springboot.model.Usuario;
import springboot.repository.QuestaoRepository;
import springboot.util.CustomAggregationOperation;

@Service
public class QuestaoService {

	private final String errorMessage = "A questão não está cadastrada.";
	public static final int ENUNCIADO = 0;
	public static final int COMPETENCIA = 1;

	private ArrayList<String> arrayParametros = new ArrayList<String>();
	private ArrayList<String> arrayOperadores = new ArrayList<String>();
	private ArrayList<String> arrayQuery = new ArrayList<String>();
	private ArrayList<Object> parametros = new ArrayList<Object>();

	@Autowired
	private QuestaoRepository questaoRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

	public Questao save(Questao questao) throws IOException {
		
		// Aqui chama o classificador e atualiza o objeto questao
		//questao.setCompetencias(getSetCompetencias(questao.getEnunciado()));

		questaoRepository.save(questao);

		return questao;
	}
	 

	public Questao delete(String id) {
		Optional<Questao> optQuestao = questaoRepository.findById(id);

		if (!optQuestao.isPresent()) {
			throw new RegisterNotFoundException(errorMessage);
		}

		Questao questao = optQuestao.get();

		questaoRepository.delete(questao);
		return questao;
	}

	public Questao update(Questao questao, String id) throws IOException {

		Optional<Questao> optQuestao = questaoRepository.findById(id);

		if (!optQuestao.isPresent()) {
			throw new RegisterNotFoundException(errorMessage);
		}

		Questao novaQuestao = optQuestao.get();

		if (!questao.getAutor().equals(novaQuestao.getAutor())) {
			throw new PermissionDeniedException("A questão é de propriedade de outro usuário");
		}

		if (!novaQuestao.getEstado().equals(EstadoQuestao.RASCUNHO)) {
			throw new PermissionDeniedException("Uma questão definitiva não pode ser mais alterada");
		}


        questao.setId(id);

		questaoRepository.save(questao);

		return novaQuestao;
	}

	public Page<Questao> getAll(int page, int size) {

	    Pageable pageable = PageRequest.of(page, size);
	    Page<Questao> pagina = questaoRepository.findAll(pageable);
	    
		return pagina;
	}

	public Questao getById(String id) {
		Optional<Questao> optQuestao = questaoRepository.findById(id);

		if (!optQuestao.isPresent()) {
			throw new RegisterNotFoundException(errorMessage);
		}

		return optQuestao.get();
	}

	private void iniciaColecoes() {
		arrayParametros.add("{$text:{$search:");
		arrayParametros.add("{competencias:  {$all:");
		arrayParametros.add("{autor:{ $regex:");
		arrayParametros.add("{fonte:{ $regex:");
		arrayParametros.add("{tipo:{ $regex:");
		arrayParametros.add("{conteudo:{ $regex:");


		arrayOperadores.add("{'$or':[");
		arrayOperadores.add("{'$and':[");
		arrayOperadores.add("{score: {$meta: \\\"textScore\\\"}}.sort({score:{$meta:\\\"textScore\\\"}})");
	}

	public Page<Questao> getByEnunciadoCompetenciasAutorFonteTipo(String enunciado, HashSet<String> competencias,
			String autor, String fonte, String tipo, String conteudo,int page, int size) {

		iniciaColecoes();

		parametros.add(enunciado);
		if (competencias.contains("null"))
			parametros.add("null");
		else
			parametros.add(competencias);
		parametros.add(autor);
		parametros.add(fonte);
		parametros.add(tipo);
		parametros.add(conteudo);		
		
		// inicio da query com o operador lógico AND 

		String query = arrayOperadores.get(1);


		for (int i = 0; i < parametros.size(); i++) {
			if (!isNull(parametros.get(i))) {
				if (i == ENUNCIADO) {
					// Caso tenha enunciado e NÃO competência
					if (isNull(parametros.get(i + 1))) {
						// Precisa de duas chaves de fechamento e aspas
						arrayQuery.add(arrayParametros.get(i) + " '" + parametros.get(i) + "'}}");
					} 
				} else if (i == COMPETENCIA) {
					String subQuery = "";
					// Caso tenha competência E enunciado
					if (!isNull(parametros.get(i - 1))) {
												
						subQuery = arrayParametros.get(i - 1) + " '" + parametros.get(i - 1);
						
						for (String competencia : competencias) {
							subQuery += " " + competencia;
						}
					// Caso tenha competência e NÃO enunciado
					} else {
						subQuery = arrayParametros.get(i - 1) + " '";
						
						for (String competencia : competencias) {
							subQuery += " " + competencia;
						}	
					}
					
					subQuery += "'}}";	
					arrayQuery.add(subQuery);
				} else {
					// Precisa de uma chave de fechamento e aspas
					arrayQuery.add(arrayParametros.get(i) + "/" + parametros.get(i) + "/i}}");
				}
			}
		}

		query += String.join(",", arrayQuery);

		// fechamento do operador lógico AND
		query += "]}";

		System.out.println(query);
		parametros.clear();
		arrayQuery.clear();
		
	    Sort sort = Sort.by(
	    	    Sort.Order.desc("score"));
	    
	    Pageable pageable = PageRequest.of(page, size, sort);
	    
	    Page<Questao> pagina = questaoRepository.getByEnunciadoCompetenciasAutorFonteTipo(query,pageable);
	    
		return pagina;

	}

	public Set<CompetenciaType> getSetCompetencias(String enunciado) throws IOException  {
		
		String enunciadoText = extractAllText(enunciado);

		Set<CompetenciaType> competencias = new HashSet<>();

	    URL obj = new URL("https://question-classifier.herokuapp.com/classifier/");
	    HttpsURLConnection postConnection = (HttpsURLConnection) obj.openConnection();
	    postConnection.setRequestMethod("POST");
	    postConnection.setRequestProperty("Content-Type", "text/plain");
	    postConnection.setDoOutput(true);
	    OutputStream os = postConnection.getOutputStream();
	    os.write(enunciadoText.getBytes());
	    os.flush();
	    os.close();
	    int responseCode = postConnection.getResponseCode();
	        
	    
	    BufferedReader in = new BufferedReader(new InputStreamReader(
	            postConnection.getInputStream()));
	        String inputLine;
	        StringBuffer response = new StringBuffer();
	        while ((inputLine = in .readLine()) != null) {
	            response.append(inputLine);
	        } in .close();
	        
	        
	        
	        List<String> result = Arrays.asList(response.toString().split(","));


	        for (int i = 0; i < result.size(); i++) {
	        	List<String> compChave = Arrays.asList(result.get(i).split(":"));

	        	if (compChave.get(1).charAt(2) == '1') {
	        		competencias.add(getCompetencia(compChave.get(0)));
	        	}
	        }
		


		return competencias;
	}
	
	private static String extractAllText(String htmlText) {
	    Source source = new Source(htmlText);
	    return source.getTextExtractor().toString();
	}   

	private CompetenciaType getCompetencia(String chave) {
		CompetenciaType valor = null;
		for (CompetenciaType competencia : CompetenciaType.values()) {
			if (chave.contains(competencia.value)) {
				valor = competencia;
			}
		}
		return valor;
	}

	private boolean isNull(Object parametro) {
		return parametro.equals("null");
	}

	public Questao getPendente(Usuario usuario) {

        List<CustomAggregationOperation>  aggList = new ArrayList<>();
		aggList.add(new CustomAggregationOperation(Document.parse(
			"{\n" +
			"    \"$lookup\": {\n" +
			"        \"from\": \"avaliacao\",\n" +
			"        let: { id: { \"$toString\": \"$_id\" } },\n" +
			"        \"pipeline\": [{\n" +
			"            \"$match\": {\n" +
			"                \"$expr\": {\n" +
			"                    \"$and\": [\n" +
			"                        { \"$eq\": [ \"$$id\", \"$questao\" ] },\n" +
			"                        { \"$eq\": [ \"$autor\", \"" + usuario.getEmail() + "\" ] }\n" +
			"                    ]\n" +
			"                }\n" +
			"            }\n" +
			"        },\n" +
			"        { $limit: 1 }],\n" +
			"        \"as\": \"avaliacao\"\n" +
			"    }\n" +
			"}"
		)));
        aggList.add(new CustomAggregationOperation(Document.parse("{ \"$match\": { \"qtdAvaliacoes\": { \"$lt\": 3 } } }")));
		aggList.add(new CustomAggregationOperation(Document.parse("{ \"$match\": { \"avaliacao._id\": { \"$exists\": false } } }")));
		aggList.add(new CustomAggregationOperation(Document.parse("{ \"$sort\" : { \"ultimoAcesso\" : 1 } }")));
		aggList.add(new CustomAggregationOperation(Document.parse("{ \"$limit\" : 1 }")));
		aggList.add(new CustomAggregationOperation(Document.parse("{ \"$project\": { \"avaliacao\": false } }")));
		Questao melhorQuestao;
		try {
			Aggregation agg = Aggregation.newAggregation(aggList);
			List<Questao> results = mongoTemplate.aggregate(agg, "questao", Questao.class).getMappedResults();
			melhorQuestao = results.get(0);
			melhorQuestao.setUltimoAcesso(System.currentTimeMillis());
			update(melhorQuestao, melhorQuestao.getId());
		} catch(Exception e) {
			try {
				aggList.remove(1);
				Aggregation agg = Aggregation.newAggregation(aggList);
				List<Questao> results = mongoTemplate.aggregate(agg, "questao", Questao.class).getMappedResults();
				melhorQuestao = results.get(0);
				melhorQuestao.setUltimoAcesso(System.currentTimeMillis());
				update(melhorQuestao, melhorQuestao.getId());
			} catch (Exception e2) {
				throw new NoPendentQuestionException("Não existe nenhuma questão pendente de avaliação");
			}
		}

		return melhorQuestao;
	}

}
