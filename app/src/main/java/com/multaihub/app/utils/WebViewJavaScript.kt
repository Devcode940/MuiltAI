package com.multaihub.app.utils

/**
 * JavaScript utilities for WebView injection in the MultiAI Hub application.
 * Contains pre-defined JavaScript code for AI chat interfaces.
 */
object WebViewJavaScript {
    
    /**
     * JavaScript to inject into WebView for basic AI chat functionality.
     * Handles message submission and response display.
     */
    const CHAT_INIT_SCRIPT = """
        (function() {
            const chatContainer = document.createElement('div');
            chatContainer.id = 'multi-ai-chat-container';
            chatContainer.style.fontFamily = 'Arial, sans-serif';
            chatContainer.style.padding = '16px';
            chatContainer.style.maxWidth = '800px';
            chatContainer.style.margin = '0 auto';
            
            const messageArea = document.createElement('div');
            messageArea.id = 'multi-ai-messages';
            messageArea.style.marginBottom = '16px';
            messageArea.style.maxHeight = '60vh';
            messageArea.style.overflowY = 'auto';
            messageArea.style.border = '1px solid #ddd';
            messageArea.style.borderRadius = '8px';
            messageArea.style.padding = '16px';
            
            const inputArea = document.createElement('div');
            inputArea.style.display = 'flex';
            inputArea.style.gap = '8px';
            
            const input = document.createElement('textarea');
            input.id = 'multi-ai-input';
            input.style.flex = '1';
            input.style.padding = '12px';
            input.style.border = '1px solid #ddd';
            input.style.borderRadius = '8px';
            input.style.fontSize = '16px';
            input.placeholder = 'Type your message...';
            input.rows = 2;
            
            const sendButton = document.createElement('button');
            sendButton.id = 'multi-ai-send';
            sendButton.textContent = 'Send';
            sendButton.style.padding = '12px 24px';
            sendButton.style.backgroundColor = '#6200ee';
            sendButton.style.color = 'white';
            sendButton.style.border = 'none';
            sendButton.style.borderRadius = '8px';
            sendButton.style.cursor = 'pointer';
            sendButton.style.fontSize = '16px';
            
            inputArea.appendChild(input);
            inputArea.appendChild(sendButton);
            
            chatContainer.appendChild(messageArea);
            chatContainer.appendChild(inputArea);
            
            document.body.insertBefore(chatContainer, document.body.firstChild);
            
            window._multiAiMessages = [];
            
            window.addChatMessage = function(sender, content, isHtml = false) {
                const messageDiv = document.createElement('div');
                messageDiv.style.marginBottom = '12px';
                messageDiv.style.padding = '12px';
                messageDiv.style.borderRadius = '8px';
                messageDiv.style.maxWidth = '80%';
                
                if (sender === 'user') {
                    messageDiv.style.backgroundColor = '#6200ee';
                    messageDiv.style.color = 'white';
                    messageDiv.style.marginLeft = 'auto';
                    messageDiv.style.marginRight = '0';
                } else {
                    messageDiv.style.backgroundColor = '#f5f5f5';
                    messageDiv.style.color = '#333';
                    messageDiv.style.marginLeft = '0';
                    messageDiv.style.marginRight = 'auto';
                }
                
                if (isHtml) {
                    messageDiv.innerHTML = content;
                } else {
                    messageDiv.textContent = content;
                }
                
                messageArea.appendChild(messageDiv);
                messageArea.scrollTop = messageArea.scrollHeight;
                
                window._multiAiMessages.push({ sender, content });
            };
            
            window.getUserInput = function() {
                return document.getElementById('multi-ai-input').value;
            };
            
            window.clearInput = function() {
                document.getElementById('multi-ai-input').value = '';
            };
            
            window.setLoading = function(isLoading) {
                const button = document.getElementById('multi-ai-send');
                button.disabled = isLoading;
                button.textContent = isLoading ? 'Sending...' : 'Send';
            };
            
            sendButton.addEventListener('click', function() {
                const input = document.getElementById('multi-ai-input');
                const message = input.value.trim();
                if (message) {
                    window.addChatMessage('user', message);
                    window.clearInput();
                    if (window.MultiAI && window.MultiAI.onUserMessage) {
                        window.MultiAI.onUserMessage(message);
                    }
                }
            });
            
            input.addEventListener('keydown', function(e) {
                if (e.ctrlKey && e.key === 'Enter') {
                    sendButton.click();
                }
            });
            
            console.log('MultiAI WebView JavaScript initialized');
        })();
    """.trimIndent()
    
    const EXTRACT_RESPONSE_SCRIPT = """
        (function() {
            const messages = window._multiAiMessages || [];
            const aiMessages = messages.filter(m => m.sender !== 'user');
            if (aiMessages.length > 0) {
                return aiMessages[aiMessages.length - 1].content;
            }
            return null;
        })();
    """.trimIndent()
    
    const CLEAR_MESSAGES_SCRIPT = """
        (function() {
            window._multiAiMessages = [];
            const messageArea = document.getElementById('multi-ai-messages');
            if (messageArea) {
                messageArea.innerHTML = '';
            }
        })();
    """.trimIndent()
    
    const IS_INITIALIZED_SCRIPT = """
        (function() {
            return typeof window.addChatMessage === 'function';
        })();
    """.trimIndent()
}
