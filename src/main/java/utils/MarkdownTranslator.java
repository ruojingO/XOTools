package utils;

import api.translate.service.GPT4oTranslator; // Assuming this is accessible
import api.translate.service.TranslationService;
import com.vladsch.flexmark.ast.Text; // Specific node type for text
import com.vladsch.flexmark.ast.*; // For other node types like Code, FencedCodeBlock, Link, Image, HtmlBlock, HtmlCommentBlock etc.
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.NodeVisitor;
import com.vladsch.flexmark.util.ast.VisitHandler;
import com.vladsch.flexmark.util.sequence.BasedSequence; // For setting text node content
//import com.vladsch.flexmark.html.HtmlRenderer; // Or use a MarkdownRenderer if writing back to MD
import com.vladsch.flexmark.formatter.Formatter; // For rendering back to Markdown
import com.vladsch.flexmark.util.data.MutableDataSet; 

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.List;

public class MarkdownTranslator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MarkdownTranslator.class);
    private final TranslationService translationService;
    private final Parser parser;
    private final Formatter renderer; // For rendering back to Markdown

    public MarkdownTranslator(TranslationService translationService) {
        this.translationService = translationService;
        MutableDataSet options = new MutableDataSet();
        // Configure options for parser and renderer if needed
        // e.g., options.set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create(), ...));
        // For basic markdown, default options are often sufficient.
        // To ensure GFM style (like tables, strikethrough, etc.) if they exist in source:
        // options.set(Parser.EXTENSIONS, GfmExtensions.create());
        // options.set(Formatter.FORMATTER_EMULATION, Formatter.FormatterEmulation.GFM);
        this.parser = Parser.builder(options).build();
        this.renderer = Formatter.builder(options).build();
    }

    public void translateFile(String inputFilePath, String outputFilePath) throws IOException {
        LOGGER.info("Reading Markdown file: {}", inputFilePath);
        String markdownContent = new String(Files.readAllBytes(Paths.get(inputFilePath)), StandardCharsets.UTF_8);

        Node document = parser.parse(markdownContent);
        LOGGER.info("Markdown parsing complete.");

        NodeVisitor visitor = new NodeVisitor(
            new VisitHandler<>(Text.class, this::visitTextNode),
            // Explicitly skip processing for children of these node types by providing an empty handler.
            // This prevents the Text node visitor from being called for text within these blocks.
            new VisitHandler<>(Code.class, node -> {}), 
            new VisitHandler<>(FencedCodeBlock.class, node -> {}),
            new VisitHandler<>(IndentedCodeBlock.class, node -> {}),
            new VisitHandler<>(HtmlBlock.class, node -> {}),
            new VisitHandler<>(HtmlCommentBlock.class, node -> {}),
            new VisitHandler<>(HtmlInline.class, node -> {}),
            new VisitHandler<>(AutoLink.class, node -> {}),
            // For Links and Images, the default behavior is to visit their children.
            // The 'Text' content of a Link or the 'alt text' of an Image are themselves Text nodes.
            // The URL parts are not Text nodes, so they won't be visited by visitTextNode.
            // We add specific checks in visitTextNode if the Text node itself is a URL (e.g. if link/image text is same as URL).
            new VisitHandler<>(LinkNode.class, node -> {
                // Link text is a child Text node, which will be visited by the Text.class handler.
                // No need to explicitly call visitChildren(node) here if default traversal is desired
                // and the handler isn't meant to stop further processing of children.
                // The default behavior of NodeVisitor is to visit children *after* the node itself is processed by its handler.
                // If we want to ensure children are visited even if this handler did something,
                // then visitor.visitChildren(node) would be explicit.
                // For now, relying on default child visitation.
            }),
            new VisitHandler<>(Image.class, node -> {
                // Alt text is a child Text node.
                // Relying on default child visitation.
            })
            // Removed the explicit anonymous class with process() override.
            // The default NodeVisitor behavior will visit children of nodes not specifically handled
            // by a handler that stops propagation (like the empty handlers for Code, FencedCodeBlock, etc.).
        );
        
        visitor.visit(document);
        LOGGER.info("AST traversal and text translation processing complete.");

        String translatedMarkdown = renderer.render(document); 
        LOGGER.info("Writing translated Markdown to: {}", outputFilePath);
        Files.write(Paths.get(outputFilePath), translatedMarkdown.getBytes(StandardCharsets.UTF_8));
        LOGGER.info("Markdown translation complete for: {}", outputFilePath);
    }

    private void visitTextNode(Text textNode) {
        String originalText = textNode.getChars().toString();
        if (originalText.trim().isEmpty()) {
            return; 
        }

        Node parent = textNode.getParent();
        Node grandParent = (parent != null) ? parent.getParent() : null;

        // Skip text if it's a URL within a Link or Image node's text itself.
        // This is a more specific check for cases where the link text *is* the URL.
        if (parent instanceof LinkNode && originalText.equals(((LinkNode) parent).getUrl().toString())) {
            LOGGER.debug("Skipping Text node that is a URL within a Link: '{}'", originalText);
            return;
        }
        if (parent instanceof Image && originalText.equals(((Image) parent).getUrl().toString())) {
             LOGGER.debug("Skipping Text node that is a URL within an Image's alt text (if alt text is same as URL): '{}'", originalText);
            return;
        }
        
        // The NodeVisitor setup should already prevent visiting Text nodes within Code, FencedCodeBlock, etc.
        // However, an explicit check for direct parent can be a safeguard, though it might be redundant
        // if the visitor is correctly configured *not* to descend into those excluded blocks for Text nodes.
        // The current NodeVisitor setup with empty handlers for Code, FencedCodeBlock, etc., *should*
        // mean this visitTextNode is NOT called for their children. The key is that those handlers
        // do not call visitChildren(). Let's rely on that primarily.
        // The below loop is an additional safeguard but might be overly cautious if visitor is perfect.

        Node currentParent = textNode.getParent();
        while (currentParent != null) {
            if (currentParent instanceof Code || currentParent instanceof FencedCodeBlock || currentParent instanceof IndentedCodeBlock ||
                currentParent instanceof HtmlBlock || currentParent instanceof HtmlCommentBlock || currentParent instanceof HtmlInline ||
                currentParent instanceof AutoLink) {
                // This case should ideally not be reached if the NodeVisitor's empty handlers for these types work as expected.
                LOGGER.debug("Safeguard: Skipping Text node within an excluded parent type [{}]: '{}'", currentParent.getNodeName(), originalText);
                return;
            }
            currentParent = currentParent.getParent();
        }


        LOGGER.debug("Identified translatable Text node: '{}'", originalText);
        try {
            String translatedText = translationService.translate(originalText);
            // Small delay to be polite to the API, if this service hits an external API frequently
            Thread.sleep(200); // Consider making this configurable or part of TranslationService

            if (!originalText.equals(translatedText)) {
                // Convert the translated string to a BasedSequence.
                // The new sequence should ideally be based on the original node's sequence for source mapping,
                // but for simple text replacement, creating a new BasedSequence from the string is often sufficient.
                // Using textNode.getChars().getBaseSequence().subSequence(translatedText) might be more accurate if base is shared.
                // The correct way is to use setChars with a BasedSequence.
                textNode.setChars(BasedSequence.of(translatedText));
                LOGGER.debug("Translated '{}' to '{}'", originalText, translatedText);
            } else {
                LOGGER.debug("Text unchanged after translation (or translation returned original): '{}'", originalText);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.warn("Translation delay interrupted for text: '{}'", originalText, e);
        } catch (Exception e) {
            LOGGER.error("Failed to translate text segment: '{}'. Error: {}", originalText, e.getMessage(), e);
            // Keep original text if translation fails
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: MarkdownTranslator <inputFilePath.md> <outputFilePath_zh.md>");
            LOGGER.error("Usage: MarkdownTranslator <inputFilePath.md> <outputFilePath_zh.md>");
            return;
        }
        String inputFile = args[0];
        String outputFile = args[1];

        // Ensure OPENAI_API_KEY (and proxy settings if needed) are set as environment variables
        // for GPT4oTranslator to pick them up.
        if (System.getenv("OPENAI_API_KEY") == null || System.getenv("OPENAI_API_KEY").isEmpty()) {
            System.err.println("Error: OPENAI_API_KEY environment variable must be set for GPT4oTranslator.");
            LOGGER.error("OPENAI_API_KEY environment variable must be set for GPT4oTranslator.");
            return;
        }

        try {
            // Defaulting to GPT4oTranslator for the main method example.
            // In a real application, this might be configurable.
            TranslationService service = new GPT4oTranslator(); 
            MarkdownTranslator translator = new MarkdownTranslator(service);
            translator.translateFile(inputFile, outputFile);
            System.out.println("Markdown translation process finished for " + inputFile);
            LOGGER.info("Markdown translation process finished successfully for {}.", inputFile);
        } catch (Exception e) {
            LOGGER.error("Error during Markdown translation process for file {}: {}", inputFile, e.getMessage(), e);
            e.printStackTrace();
        }
    }
}
