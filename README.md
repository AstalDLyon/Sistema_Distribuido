# Projeto de Sistema Distribuído

## 📌 Arquitetura baseada nos princípios SOLID

Cada classe possui uma responsabilidade única e bem definida:

- **MainClient / MainServer** – Pontos de entrada do sistema
- **nomeServidor** – Lógica principal do servidor
- **Resolver** – Cliente e comunicação via sockets
- **FileManager** – Persistência de dados em arquivo
- **ConfigManager** – Configurações globais (Singleton, Thread-safe)
- **LogManager** – Classe que gerencia logs

---

## ✅ Funcionalidades

- [x] Registro de IP com nome  
- [x] Consulta com sockets  
- [x] Persistência em arquivo  
- [x] Replicação entre servidores (servidor distribuído)  
- [x] Configurações com conformidade Thread-safe  
- [x] ExecutorService para controlar acessos simultâneos  
- [x] Timeouts  
- [x] Logs

---
## Tutorial de teste dos servidores secundarios

Logo após colocar o MainServer normal(sem paramentros para rodar) siga os passos abaixo em ordem:
1. **Primeiro**, vá até o diretório do seu projeto. Você pode usar o comando seguido do caminho do projeto. Por exemplo: `cd`
   cd C:\Users\bralv\IdeaProjects\Sistema_Distribuido
2. **Depois**, compile o projeto (se ainda não compilou):
   javac -d . src/main/java/com/av2/sistemadistribuidos/*.java(seu diretorio no lugar desse exemplo)
3. **Agora**, execute o servidor secundário:
   java com.av2.sistemadistribuidos.MainServer secundario 12346
4. **Em outro terminal CMD digite em ordem para outro servidor:
   cd C:\Users\bralv\IdeaProjects\Sistema_Distribuido(seu diretorio no lugar desse exemplo)
   java com.av2.sistemadistribuidos.MainServer secundario 12347
5. Verifique se eles estão ativos:
   netstat -ano | findstr "12345 12346 12347"

Deverá aparecer algo como:
TCP    0.0.0.0:12345          0.0.0.0:0              LISTENING       4980
TCP    0.0.0.0:12346          0.0.0.0:0              LISTENING       13516
TCP    0.0.0.0:12347          0.0.0.0:0              LISTENING       10224
TCP    [::]:12345             [::]:0                 LISTENING       4980
TCP    [::]:12346             [::]:0                 LISTENING       13516
TCP    [::]:12347             [::]:0                 LISTENING       10224



Este projeto é parte de um trabalho acadêmico, e novas funcionalidades serão adicionadas ao longo do tempo.
