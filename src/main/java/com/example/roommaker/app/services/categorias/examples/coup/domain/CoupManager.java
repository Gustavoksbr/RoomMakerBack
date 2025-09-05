package com.example.roommaker.app.services.categorias.examples.coup.domain;

import com.example.roommaker.app.domain.models.Sala;
import com.example.roommaker.app.domain.exceptions.Erro404;
import com.example.roommaker.app.domain.exceptions.Erro500;
import com.example.roommaker.app.domain.exceptions.ErroDeRequisicaoGeral;
import com.example.roommaker.app.services.categorias.examples.coup.controler.request.AcaoRequest;
import com.example.roommaker.app.services.categorias.examples.coup.controler.request.RespostaCoupRequest;
import com.example.roommaker.app.services.categorias.examples.coup.domain.model.Coup;
import com.example.roommaker.app.services.categorias.examples.coup.domain.model.JogadorCoup;
import com.example.roommaker.app.services.categorias.examples.coup.domain.model.SalaDosCoups;
import com.example.roommaker.app.services.categorias.examples.coup.domain.model.Turno;
import com.example.roommaker.app.services.categorias.examples.coup.domain.model.main.Acao;
import com.example.roommaker.app.services.categorias.examples.coup.domain.model.main.Bloqueio;
import com.example.roommaker.app.services.categorias.examples.coup.domain.model.main.Personagem;
import com.example.roommaker.app.services.categorias.examples.coup.domain.model.main.StatusCoup;
import com.example.roommaker.app.services.categorias.examples.coup.repository.CoupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CoupManager {

    private final CoupRepository coupRepository;

    private final List<Acao> getAcoesDuvidaveis = Arrays.asList(Acao.TAXAR, Acao.TROCAR_INFLUENCIAS, Acao.EXTORQUIR, Acao.ASSASSINAR);
    private final List<Acao> acoesInduvidaveis = Arrays.asList(Acao.RENDA, Acao.GOLPE_DE_ESTADO, Acao.AJUDA_EXTERNA);
    private final List<Acao> acoesBloqueaveis = Arrays.asList( Acao.TROCAR_INFLUENCIAS, Acao.EXTORQUIR, Acao.ASSASSINAR, Acao.AJUDA_EXTERNA);
    private final List<Acao> acoesNaoBloqueaveis = Arrays.asList(Acao.RENDA, Acao.GOLPE_DE_ESTADO, Acao.TAXAR);
    private final List<Acao> acoesComAlvo = Arrays.asList(Acao.EXTORQUIR, Acao.ASSASSINAR, Acao.GOLPE_DE_ESTADO);

private final Stack<Personagem> baralho = new Stack<>();
{
    baralho.addAll(Arrays.asList(Personagem.DUQUE, Personagem.DUQUE, Personagem.DUQUE, Personagem.CAPITAO, Personagem.CAPITAO, Personagem.CAPITAO, Personagem.CONDESSA, Personagem.CONDESSA, Personagem.CONDESSA, Personagem.ASSASSINO, Personagem.ASSASSINO, Personagem.ASSASSINO, Personagem.EMBAIXADOR, Personagem.EMBAIXADOR, Personagem.EMBAIXADOR));
}
    // todos os bloqueios sao duvidaveis


    @Autowired
    public CoupManager(CoupRepository coupRepository) {
        this.coupRepository = coupRepository;
    }
    public void criarSalaDeJogo(Sala sala){
        SalaDosCoups salaDosCoups = new SalaDosCoups(sala);
        this.coupRepository.criarSalaDosCoups(salaDosCoups);
    }

    public void olar(){
        System.out.println("olar");
    }
    public Coup iniciarJogo(Sala sala){
        if(sala.getUsernameParticipantes() == null || sala.getUsernameParticipantes().isEmpty()){
            throw new ErroDeRequisicaoGeral("Número de jogadores insuficiente");
        }
        SalaDosCoups salaDosCoups = this.coupRepository.buscarSalaDosCoups(sala.getNome(), sala.getUsernameDono());
        if (salaDosCoups.getCoupAtual()!=null){
            throw new ErroDeRequisicaoGeral("Jogo já iniciado");
        }
        Coup coup = new Coup();
        coup.setId(salaDosCoups.getHistoricoCoups().size()+1L);
        coup.setJogadores(new ArrayList<>());

        for (String username : sala.getUsernameParticipantes()){
            JogadorCoup jogadorCoup = new JogadorCoup();
            jogadorCoup.setUsername(username);
            coup.getJogadores().add(jogadorCoup);
        }
        JogadorCoup jogadorCoupDono = new JogadorCoup();
        jogadorCoupDono.setUsername(sala.getUsernameDono());
        coup.getJogadores().add(jogadorCoupDono);
       coup.setUsernameJogadoresVivos(new ArrayList<>(sala.getUsernameParticipantes()));
       coup.getUsernameJogadoresVivos().add(sala.getUsernameDono());
       coup.setUsernameJogadoresMortos(new ArrayList<>());


        Collections.shuffle(coup.getJogadores());

        coup = embaralharCartas(coup, baralho);



        for (JogadorCoup jogadorCoup : coup.getJogadores()){
            jogadorCoup.setInfluencias(new ArrayList<>());
            for (int i = 0; i < 2; i++){
                jogadorCoup.getInfluencias().add(coup.getBaralho().pop());
            }
        }
        coup = novoTurno(coup);
        salaDosCoups.setCoupAtual(coup);
        System.out.println(coup.toString());

       return this.coupRepository.salvarSalaDosCoups(salaDosCoups).getCoupAtual();
    }

    private Coup novoTurno(Coup coup){
        Turno turno = new Turno();
//        Turno turnoAnterior = coup.getTurnoAtual();
//
//        if(turnoAnterior!=null){
//            if(turnoAnterior.getStatus()==StatusCoup.FINALIZADO){
//                turno.setOrdem(turnoAnterior.getOrdem()+1);
//            }else{
//                throw new ErroDeRequisicaoGeral("Turno anterior não finalizado");
//            }
//        }else {
//            turno.setOrdem(1L);
//        }
        int tamanho;
        int vezAtual;
        if(coup.getTurnos()==null){
            coup.setTurnos(new ArrayList<>());
            tamanho = 0;
            vezAtual = 0;
        }else {
            tamanho = coup.getTurnos().size();
            vezAtual =  (tamanho % coup.getJogadores().size());
        }

        turno.setJogador(coup.getJogadores().get(vezAtual));
        turno.setOrdem(tamanho+1L);

        turno.setStatus(StatusCoup.PRIMEIRA_ETAPA_ESCOLHER_JOGADA);
        coup.setTurnoAtual(turno);
        return coup;
    }



    private Coup embaralharCartas(Coup coup, Stack<Personagem> cartasASeremEmbaralhadas){
        Stack<Personagem> baralhoEmbaralhado = new Stack<>();
        baralhoEmbaralhado.addAll(cartasASeremEmbaralhadas);
        Collections.shuffle(baralhoEmbaralhado);
        coup.setBaralho(baralhoEmbaralhado);
        return coup;
    }

    public Coup realizarAcao(String nomeSala, String usernameDono, String usernameJogador, AcaoRequest acaoRequest) {
        SalaDosCoups salaDosCoups = this.coupRepository.buscarSalaDosCoups(nomeSala, usernameDono);
        Coup coup = salaDosCoups.getCoupAtual();
        if(coup==null){
            throw new ErroDeRequisicaoGeral("Jogo não iniciado");
        }
        Turno turnoEncontrado = coup.getTurnoAtual();

        if(!Objects.equals(usernameJogador, turnoEncontrado.getJogador().getUsername())){
            throw new ErroDeRequisicaoGeral("Não é a vez do jogador");
        }

        if(turnoEncontrado.getAcao()!=null){
            throw new ErroDeRequisicaoGeral("Ação já realizada");
        }
        if(turnoEncontrado.getStatus() != StatusCoup.PRIMEIRA_ETAPA_ESCOLHER_JOGADA){
            throw new ErroDeRequisicaoGeral("Não é a etapa de escolher ação");
        }
        if(coup.getTurnoAtual().getJogador().getMoedas() >= 10 && acaoRequest.getAcao() != Acao.GOLPE_DE_ESTADO){
            throw new ErroDeRequisicaoGeral("Você tem 10 ou mais moedas, você deve realizar um golpe de estado");
        }
        coup.setTurnoAtual(exigirAcao(coup,acaoRequest));
        salaDosCoups.setCoupAtual(coup);
        this.coupRepository.salvarSalaDosCoups(salaDosCoups);
        return coup;
    }
    public Coup duvidarAcaoInicial(String nomeSala, String usernameDono, String usernameJogador ){
        SalaDosCoups salaDosCoups = this.coupRepository.buscarSalaDosCoups(nomeSala, usernameDono);
        Coup coup = salaDosCoups.getCoupAtual();
        if(coup==null){
            throw new ErroDeRequisicaoGeral("Jogo não iniciado");
        }
        Turno turno = coup.getTurnoAtual();
        if(turno.getAcao()==null){
            throw new ErroDeRequisicaoGeral("Não há ação a ser duvidada");
        }
        if(Objects.equals(usernameJogador, turno.getJogador().getUsername())){
            throw new ErroDeRequisicaoGeral("Você não pode duvidar sua própria ação");
        }
        if(turno.getBloqueadoPor()!=null || turno.getDuvidadoPor()!=null){
            throw new ErroDeRequisicaoGeral("Ação já bloqueada/duvidada");
        }
       coup.setTurnoAtual(this.validarDuvidaInicial(turno));
        String username = turno.getJogador().getUsername();
        JogadorCoup jogadorDoTurno =  coup.getJogadores().stream().filter(jogadorCoup -> Objects.equals(jogadorCoup.getUsername(), username)).findFirst().orElseThrow(() -> new Erro404("Jogador não encontrado"));
        jogadorDoTurno = turno.getJogador();

        String usernameDeQuemDuvidou = turno.getDuvidadoPor().getUsername();
        JogadorCoup jogadorQueDuvidou = coup.getJogadores().stream().filter(jogadorCoup -> Objects.equals(jogadorCoup.getUsername(), usernameJogador)).findFirst().orElseThrow(() -> new Erro404("Jogador não encontrado"));
        jogadorQueDuvidou = turno.getDuvidadoPor();


        this.coupRepository.salvarSalaDosCoups(salaDosCoups);
        return coup;
    }

    public void bloquear(String nomeSala, String usernameDono, String usernameJogador, Bloqueio bloqueio) {
        SalaDosCoups salaDosCoups = this.coupRepository.buscarSalaDosCoups(nomeSala, usernameDono);
        Coup coup = salaDosCoups.getCoupAtual();
        if(coup==null){
            throw new ErroDeRequisicaoGeral("Jogo não iniciado");
        }
        Turno turno = coup.getTurnoAtual();
        if(turno.getAcao()==null){
            throw new ErroDeRequisicaoGeral("Não há ação a ser bloqueada");
        }
        if(Objects.equals(usernameJogador, turno.getJogador().getUsername())){
            throw new ErroDeRequisicaoGeral("Você não pode bloquear sua própria ação");
        }
        if(turno.getBloqueadoPor()!=null || turno.getDuvidadoPor()!=null){
            throw new ErroDeRequisicaoGeral("Ação já bloqueada/duvidada");
        }
        if(!acoesBloqueaveis.contains(turno.getAcao())){
            throw new ErroDeRequisicaoGeral("Ação não bloqueável");
        }
        coup.setTurnoAtual(this.validarBloqueio(turno));
        this.coupRepository.salvarSalaDosCoups(salaDosCoups);
    }

    private Turno exigirAcao(Coup coup, AcaoRequest lance) {
        Turno turno = coup.getTurnoAtual();
        if(acoesComAlvo.contains(lance.getAcao())){
            if(lance.getAlvo()==null){
                throw new ErroDeRequisicaoGeral("Ação requer alvo");
            }
            List<JogadorCoup> jogadores = coup.getJogadores();
            jogadores.remove(coup.getTurnoAtual().getJogador());
            turno.setAcao(lance.getAcao());

            JogadorCoup alvo = jogadores.stream()
                .filter(jogadorCoup -> Objects.equals(jogadorCoup.getUsername(), lance.getAlvo()))
                .findFirst()
                .orElseThrow(() -> new ErroDeRequisicaoGeral("Alvo não encontrado no jogo"));

            turno.setAlvo(alvo);

        }
        switch (lance.getAcao()) {
            case EXTORQUIR:
                // Add your logic here
                break;
            case ASSASSINAR:
                // Add your logic here
                if(turno.getJogador().getMoedas()<3){
                    throw new ErroDeRequisicaoGeral("Moedas insuficientes");
                }
                break;
            case GOLPE_DE_ESTADO:
                if(turno.getJogador().getMoedas()<7){
                    throw new ErroDeRequisicaoGeral("Moedas insuficientes");
                }
                // Add your logic here
            case RENDA:
                // Add your logic here
                case AJUDA_EXTERNA:
                // Add your logic here
                case TAXAR:
                // Add your logic here
                case TROCAR_INFLUENCIAS:
                // Add your logic here

                break;
        }
        turno.setAcao(lance.getAcao());
        turno.setStatus(StatusCoup.SEGUNDA_ETAPA_DUVIDAR_OU_BLOQUEAR_ESCOLHA);
        return turno;
    }

    private Coup trocarCartas(String nomeSala, String usernameDono, String usernameJogador, JogadorCoup jogador){
        SalaDosCoups salaDosCoups = this.coupRepository.buscarSalaDosCoups(nomeSala, usernameDono);
        Coup coup = salaDosCoups.getCoupAtual();
        if(coup==null) {
            throw new ErroDeRequisicaoGeral("Jogo não iniciado");
        }
        Turno turno = coup.getTurnoAtual();
        if( turno == null){
            throw new ErroDeRequisicaoGeral("Turno nao iniciado");
        }
        if(turno.getStatus() != StatusCoup.QUARTA_ETAPA_ESCOLHER_CARTA_DO_BARALHO){
            throw new ErroDeRequisicaoGeral("Não é a etapa de escolher carta do baralho");
        }
        JogadorCoup jogadorCoupEncontrado = coup.getJogadores().stream().filter(jogadorCoup -> Objects.equals(jogadorCoup.getUsername(), usernameJogador)).findFirst().orElseThrow(() -> new Erro404("Jogador não encontrado"));

        if(!new HashSet<>(jogadorCoupEncontrado.getInfluencias()).containsAll(jogador.getInfluencias())){
            throw new ErroDeRequisicaoGeral("Cartas inválidas. Você deve escolher ficar com duas das quatro cartas");
        }
        List<Personagem> influenciasDescartadas = new ArrayList<>(jogadorCoupEncontrado.getInfluencias());
        influenciasDescartadas.removeAll(jogador.getInfluencias());

        jogadorCoupEncontrado.setInfluencias(jogador.getInfluencias());
        coup.getBaralho().addAll(influenciasDescartadas);
        coup = this.finalizarTurno(coup);
        return coup;
    }

    private Coup finalizarTurno(Coup coup){
            Turno turno = coup.getTurnoAtual();
            turno.setStatus(StatusCoup.FINALIZADO);
            coup.getTurnos().add(turno);
            return coup;
    }

    public Integer responder(RespostaCoupRequest respostaCoupRequest, String nomeSala, String usernameDono, String usernameJogador){
        SalaDosCoups salaDosCoups = this.coupRepository.buscarSalaDosCoups(nomeSala, usernameDono);
        Coup coup = salaDosCoups.getCoupAtual();
        if(coup==null) {
            throw new ErroDeRequisicaoGeral("Jogo não iniciado");
        }
        Turno turno = coup.getTurnoAtual();
        if( turno == null){
            throw new ErroDeRequisicaoGeral("Turno nao iniciado");
        }
        if(turno.getStatus() != StatusCoup.SEGUNDA_ETAPA_DUVIDAR_OU_BLOQUEAR_ESCOLHA){
            throw new ErroDeRequisicaoGeral("Não é a etapa de duvidar ou bloquear");
        }
        if(turno.getJogador().getUsername().equals(usernameJogador)){
            throw new ErroDeRequisicaoGeral("Você não pode responder a sua própria ação");
        }
        switch (respostaCoupRequest.getResposta()){
            case BLOQUEAR:
                turno.setBloqueadoPor(coup.getJogadores().stream().filter(jogadorCoup -> Objects.equals(jogadorCoup.getUsername(), usernameJogador)).findFirst().orElseThrow(() -> new Erro500("Jogador não encontrado")));
                break;
            case DUVIDAR:
                turno.setDuvidadoPor(coup.getJogadores().stream().filter(jogadorCoup -> Objects.equals(jogadorCoup.getUsername(), usernameJogador)).findFirst().orElseThrow(() -> new Erro500("Jogador não encontrado")));
                break;
            case ACEITAR:
              coup =  this.aceitarAcao(coup, usernameJogador);
//                turno.getSkip().add(usernameJogador);
//                if(turno.getSkip().size()==coup.getJogadores().size()-1){
//                    turno.setStatus(StatusCoup.TERCEIRA_ETAPA_RESOLVER_ACAO);
//                }
                break;
        }
        coup.setTurnoAtual(turno);
        salaDosCoups.setCoupAtual(coup);
        this.coupRepository.salvarSalaDosCoups(salaDosCoups);
        return 1;
    }
    private Coup aceitarAcao(Coup coup, String usernameJogador){
        Turno turno = coup.getTurnoAtual();
        turno.getSkip().add(usernameJogador);
        if (turno.getSkip().size() == coup.getUsernameJogadoresVivos().size()-1){
           coup = this.resolverAcao(coup);
        }
        return coup;
    }
    private Coup resolverAcao(Coup coup){
    Acao acao = coup.getTurnoAtual().getAcao();
    JogadorCoup jogador = coup.getTurnoAtual().getJogador();
    JogadorCoup alvo = coup.getTurnoAtual().getAlvo();
        switch (acao){
            case EXTORQUIR:
                Integer moedasDoAlvo = alvo.getMoedas();
                if(moedasDoAlvo>1){
                    alvo.setMoedas(moedasDoAlvo-2);
                    jogador.setMoedas(jogador.getMoedas()+2);
                }
                else {
                    alvo.setMoedas(0);
                    jogador.setMoedas(jogador.getMoedas()+moedasDoAlvo);
                }
                break;
            case ASSASSINAR:
                jogador.setMoedas(jogador.getMoedas()-3);
                alvo.getInfluencias().remove(new Random().nextInt(2));
                break;
            case GOLPE_DE_ESTADO:
                jogador.setMoedas(jogador.getMoedas()-7);
                alvo.getInfluencias().remove(new Random().nextInt(2));
                break;
            case RENDA:
                jogador.setMoedas(jogador.getMoedas()+1);
                break;
            case AJUDA_EXTERNA:
                jogador.setMoedas(jogador.getMoedas()+2);
                break;
            case TAXAR:
                jogador.setMoedas(jogador.getMoedas()+3);
                break;
            case TROCAR_INFLUENCIAS:
                List<Personagem> influenciasJogador = jogador.getInfluencias();
                List<Personagem> cartasTiradasDoBaralho = new ArrayList<>();
                for (int i = 0; i < 2; i++){
                    cartasTiradasDoBaralho.add(coup.getBaralho().pop());
                }
                influenciasJogador.addAll(cartasTiradasDoBaralho);
                coup.getTurnoAtual().setStatus(StatusCoup.QUARTA_ETAPA_ESCOLHER_CARTA_DO_BARALHO);
                break;
        }

        if(EnumSet.of(Acao.EXTORQUIR, Acao.RENDA, Acao.AJUDA_EXTERNA, Acao.TAXAR).contains(acao)){
            if(verificarDezMoedas(coup)){
                coup.getTurnoAtual().setStatus(StatusCoup.PRIMEIRA_ETAPA_ESCOLHER_JOGADA); // volta para a primeira etapa para realizar obrigatoriamente o golpe de estado
            }
            else if(acao!=Acao.TROCAR_INFLUENCIAS){
                coup.getTurnoAtual().setStatus(StatusCoup.FINALIZADO);
            }
        }

        return coup;
    }

    private Boolean verificarDezMoedas(Coup coup){
        return coup.getTurnoAtual().getJogador().getMoedas()>=10;
    }

    private Turno validarBloqueio(Turno turno){
        return null;
    }
    private Turno validarDuvidaInicial(Turno turno){
        Acao acao = turno.getAcao();
        JogadorCoup jogador = turno.getJogador();
        JogadorCoup duvidadoPor = turno.getDuvidadoPor();
        int numeroAleatorio = new Random().nextInt(2);
        switch (acao) {
            case EXTORQUIR:
                if (jogador.getInfluencias().contains(Personagem.CAPITAO)) {
                    duvidadoPor.getInfluencias().remove(numeroAleatorio);
                } else {
                    jogador.getInfluencias().remove(numeroAleatorio);
                }
            case ASSASSINAR:
                if (jogador.getInfluencias().contains(Personagem.ASSASSINO)) {
                    duvidadoPor.getInfluencias().remove(numeroAleatorio);
                } else {
                    jogador.getInfluencias().remove(numeroAleatorio);
                }
                break;
            case TAXAR:
                if (jogador.getInfluencias().contains(Personagem.DUQUE)) {
                    duvidadoPor.getInfluencias().remove(numeroAleatorio);
                } else {
                    jogador.getInfluencias().remove(numeroAleatorio);
                }
                break;
            case TROCAR_INFLUENCIAS:
                if(duvidadoPor.getInfluencias().contains(Personagem.EMBAIXADOR)){
                    duvidadoPor.getInfluencias().remove(numeroAleatorio);
                    turno.setStatus(StatusCoup.QUARTA_ETAPA_ESCOLHER_CARTA_DO_BARALHO);
                }else {
                    jogador.getInfluencias().remove(numeroAleatorio);
                }
                break;
        }
        return turno;
    }

    public Turno duvidarDoBloqueio (Turno turno){
        Acao acao = turno.getAcao();
        JogadorCoup jogadorBloqueando = turno.getBloqueadoPor();
        JogadorCoup jogador = turno.getJogador();
        JogadorCoup duvidadoPor = turno.getBloqueioDuvidadoPor();
        int numeroAleatorio = new Random().nextInt(2);
        switch (acao) {

        }
        return turno;
    }

    public void adicionarJogador(String usernameParticipante, Sala sala){
        SalaDosCoups salaDosCoups = this.coupRepository.buscarSalaDosCoups(sala.getNome(), sala.getUsernameDono());
        Coup coup = salaDosCoups.getCoupAtual();
        if(coup!=null){
            throw new ErroDeRequisicaoGeral("Jogo já iniciado");
        }
        salaDosCoups.getJogadores().add(usernameParticipante);
        this.coupRepository.salvarSalaDosCoups(salaDosCoups);
    }

    public void saidaDeParticipante(String usernameParticipante, Sala sala){
        SalaDosCoups salaDosCoups = this.coupRepository.buscarSalaDosCoups(sala.getNome(), sala.getUsernameDono());
        salaDosCoups.getJogadores().remove(usernameParticipante);
    }
}
