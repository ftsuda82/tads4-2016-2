/*
 * The MIT License
 *
 * Copyright 2016 fernando.tsuda.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package br.senac.tads4.lojinha.listener;

import br.senac.tads4.lojinha.entidade.UsuarioSistema;
import br.senac.tads4.lojinha.managedbean.AutenticacaoBean;
import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author fernando.tsuda
 */
public class AutorizacaoFilter implements PhaseListener {

  @Override
  public void afterPhase(PhaseEvent event) {
    
    FacesContext fc = event.getFacesContext();
    
    AutenticacaoBean autenticacaoBean = fc.getApplication()
	    .evaluateExpressionGet(fc, "#{autenticacaoBean}", 
		    AutenticacaoBean.class);
    
    String paginaAtual = fc.getViewRoot().getViewId();
    
    NavigationHandler nh = fc.getApplication()
	    .getNavigationHandler();
    
    // Filtrando acessos às páginas protegidas dentro do /admin
    if (paginaAtual.contains("/admin/")) {
      
      // Verificar se usuario autenticou-se
      if (autenticacaoBean == null 
	      || autenticacaoBean.getUsuario() == null) {
	// Significa que usuário nao fez autenticação
	nh.handleNavigation(fc, null, "/login.xhtml?faces-redirect=true");
	return;
      }
      
      // Significa que usuário fez autenticação.
      // Validar se usuário tem permissões necessárias para acessar a página.
      if (!verificarAcesso(autenticacaoBean.getUsuario(), 
	      paginaAtual)) {
	nh.handleNavigation(fc, null, 
		"/erro-nao-autorizado.xhtml?faces-redirect=true");
	return;
      }
      // Se chegou neste ponto, JSF prossegue com o processamento
      // normal da requisição.
    }

    
    HttpServletRequest req
	    = (HttpServletRequest) fc.getExternalContext()
		    .getRequest();
    System.out.println("Pagina atual: " + paginaAtual
	    + " ip:" + req.getRemoteAddr()); 
    
//    if (paginaAtual == null || !paginaAtual.contains("login.xhtml")) {
//      NavigationHandler nh = fc
//	    .getApplication().getNavigationHandler();
//      nh.handleNavigation(fc, null, "/login.xhtml?faces-redirect=true");
//    }
  }

  @Override
  public void beforePhase(PhaseEvent event) {

  }

  @Override
  public PhaseId getPhaseId() {
    return PhaseId.RESTORE_VIEW;
  }
  
  private static 
	boolean verificarAcesso(UsuarioSistema usuario,
		String pagina) {
	  
    // Verifica se esta acessando a página produto-form e
    // tem perfil ADMIN
    if (pagina.lastIndexOf("produto-form.xhtml") > -1
	    && usuario.autorizado("ADMIN")) {
      return true;
      
    // Verifica se tem perfil COMUM, mínimo para acessar
    // ambiente /admin
    } else if (pagina.lastIndexOf("pagina-admin.xhtml") > -1
	    && usuario.autorizado("COMUM")) {
      return true;
    }
    return false;
  }

}
