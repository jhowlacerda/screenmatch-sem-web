package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.DadosEpisodio;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";
    public void exibeMenu(){

        System.out.println("Digite o nome da série para buscar");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        System.out.println(dados);

        		List<DadosTemporada> temporadas = new ArrayList<>();

		for (int i = 1; i<=dados.totalTemporadas(); i++){
			json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") +"&season="+ i + API_KEY);
			DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
			temporadas.add(dadosTemporada);
		}
		temporadas.forEach(System.out::println);

        for(int i = 0; i< dados.totalTemporadas(); i++){
            List<DadosEpisodio> episodiosTemporada = temporadas.get(i).episodios();
            for(int j=0;j<episodiosTemporada.size();j++){
                System.out.println(episodiosTemporada.get(j).titulo());
            }

        }
        temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));



//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        //Sorted serve para ordenar.
        //O método map() do Stream em Java é uma operação intermediária que transforma cada elemento de um Stream em outro elemento de acordo com a função fornecida.
        //Em outras palavras, ele mapeia cada elemento de entrada para um novo valor, criando um novo Stream com os elementos transformados.
        //Use map() quando você está mapeando cada elemento de um Stream para exatamente um outro valor.
        List<String> nomes = Arrays.asList("Jacque", "Iasmin", "Paulo", "Rodrigo", "Nico");

        nomes.stream()
                .sorted()
                .limit(3)
                .filter(n -> n.startsWith("N"))
                .map(n -> n.toUpperCase())
                .forEach(System.out::println);



//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        //O peek usado abaixo serve como um registro de valores(logging), sem interferir na sequencia principal das operações
        // Usamos isso para Debugar, entender melhor oq esta acontecendo
        List<DadosEpisodio> dadosEpisodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream())
                .collect(Collectors.toList());


        System.out.println("\n Top 10 episodios:");
        dadosEpisodios.stream()
                .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
                .peek(e-> System.out.println("Primeiro filtro(N/A) "+e))
                .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
                .peek(e-> System.out.println("Ordenação "+e))
                .limit(10)
                .peek(e-> System.out.println("Limite "+e))
                .map(e -> e.titulo().toUpperCase())
                .peek(e-> System.out.println("Mapeamento "+e))
                .forEach(System.out::println);



        List<Episodio> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()
                        .map(d -> new Episodio(t.numero(), d))
                        ).collect(Collectors.toList());

        episodios.forEach(System.out::println);




//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        //FindFirst é uma ferramenta final, e retorna sempre um objeto do tipo Optinal.
        //Por conta disso é necessario usar o isPresent
        //o toUpperCaso no titulo e no trecho é para nao tem problemas de digitação entre letras maiusculas e minusculas
        System.out.println("Digite um trecho do titulo do episodio");
        var trechoTitulo = leitura.nextLine();
        Optional<Episodio> episodioBuscado = episodios.stream()
                .filter(e -> e.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase()))
                .findFirst();
        if (episodioBuscado.isPresent()){
            System.out.println("Episodio encontrado!");
            System.out.println("Temporada: "+ episodioBuscado.get().getTemporada());
        }else {
            System.out.println("Episodio não encontrado");
        }




//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // Filter e ForEach
        // Busca a partir de um ano
        System.out.println("A partir de que ano voce deseja ver os episodios? ");

        var ano = leitura.nextInt();
        leitura.nextLine();

        LocalDate dataBusca = LocalDate.of(ano, 1, 1);

        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        episodios.stream()
                .filter(e ->e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
                .forEach(e -> System.out.println(
                        "Temporada: " + e.getTemporada() +
                                " Episodio: " +e.getTitulo() +
                                " Data lançamento: "+ e.getDataLancamento().format(formatador)
                ));



//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // O método .collect() do Stream em Java é utilizado para transformar ou agregar os elementos de um stream em uma coleção,
        // em um único valor, ou em outro tipo de estrutura de dados. Ele é uma operação terminal, o que significa que após o seu uso, o stream não pode ser reutilizado.
        // O método .collect() usa um Collector, que é uma estratégia que define como os elementos do stream serão acumulados ou agrupados.
        // O Collector pode ser fornecido diretamente, ou você pode usar um dos coletores prontos disponíveis na classe Collectors.

        Map<Integer, Double> avaliacoesPorTemporada = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.groupingBy(Episodio::getTemporada,
                        Collectors.averagingDouble(Episodio::getAvaliacao)));
        System.out.println(avaliacoesPorTemporada);



//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------

        //o DoubleSmmaryStatistics serve justamente para extrair estastisticas, ex: DoubleSummaryStatistics{count=67, sum=588,200000, min=5,500000, average=8,779104, max=9,900000}
        DoubleSummaryStatistics est = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));
        System.out.println("Média: "+ est.getAverage());
        System.out.println("Melhor episodio: "+ est.getMax());
        System.out.println("Pior episodio: "+ est.getMin());
        System.out.println("Quantidade: "+est.getCount());


//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------

        //FlatMap
        //O método flatMap é uma operação intermediária que é usada para transformar um Stream de coleções em um Stream de elementos
        //É basicamente usado para transformar cada elemento de um Stream em outro Stream
        //Neste exemplo a seguir, transformamos um Stream de List para um Stream de Strings.
        //Use flatMap() quando você está mapeando cada elemento para um Stream, coleção ou array, e precisa "achatar" esses resultados em um único Stream.
        List<List<String>> list = List.of(
                List.of("a", "b"),
                List.of("c", "d")
        );

        Stream<String> stream = list.stream()
                .flatMap(Collection::stream);

        stream.forEach(System.out::println);


//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------

        //Reduce
        //Stream.reduce() é uma operação terminal que é utilizada
        // para reduzir o conteúdo de um Stream para um único valor.
        List<Integer> numbers = List.of(1, 2, 3, 4, 5);
        Optional<Integer> result = numbers.stream().reduce(Integer::sum);
        result.ifPresent(System.out::println);//prints 15
    }

}
