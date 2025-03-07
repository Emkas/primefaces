/**
 * __PrimeFaces Inplace Widget__
 *
 * Inplace provides easy inplace editing and inline content display. Inplace
 * consists of two members, a display element that is the initially clickable
 * label and an inline element that is the hidden content which is displayed
 * when the display element is toggled.
 *
 * @typedef {"fade" | "none" | "slide"} PrimeFaces.widget.Inplace.EffectType Available effect types for the transition
 * between the display and the inline content of the inline widget.
 *
 * @prop {JQuery} content The DOM element with the container of the label that is shown when the content or inplace
 * editor is hidden.
 * @prop {JQuery} display The DOM element with the container of the content or inplace editor that is shown when this
 * inline widget is toggled.
 * @prop {JQuery} editor The DOM element with the inplace editor, if one exists.
 *
 * @interface {PrimeFaces.widget.InplaceCfg} cfg The configuration for the {@link  Inplace| Inplace widget}.
 * You can access this configuration via {@link PrimeFaces.widget.BaseWidget.cfg|BaseWidget.cfg}. Please note that this
 * configuration is usually meant to be read-only and should not be modified.
 * @extends {PrimeFaces.widget.BaseWidgetCfg} cfg
 *
 * @prop {boolean} cfg.disabled Whether this inplace widget is disabled. If disabled, switching to the content or
 * inplace editor is not possible.
 * @prop {boolean} cfg.editor `true` to add save and cancel buttons to the inline content, or `false` otherwise. Usually
 * used when the inline content is a form field.
 * @prop {PrimeFaces.widget.Inplace.EffectType} cfg.effect Effect to be used when toggling.
 * @prop {number} cfg.effectSpeed Speed of the effect in milliseconds.
 * @prop {string} cfg.event Name of the client side event to display inline content.
 * @prop {boolean} cfg.toggleable Defines if inplace content is toggleable or not.
 */
PrimeFaces.widget.Inplace = class Inplace extends PrimeFaces.widget.BaseWidget {

    /**
     * @override
     * @inheritdoc
     * @param {PrimeFaces.PartialWidgetCfg<TCfg>} cfg
     */
    init(cfg) {
        super.init(cfg);

        this.display = $(this.jqId + '_display');
        this.content = $(this.jqId + '_content');
        this.cfg.formId = this.jq.parents('form:first').attr('id');

        var $this = this;

        if(!this.cfg.disabled) {

            if(this.cfg.toggleable) {
                // GitHub #7948 special handling for mobile
                var touchtime = 0;
                var isDoubleTap = this.cfg.event === "dblclick" && PrimeFaces.env.isTouchable(this.cfg);
                if (isDoubleTap) {
                    this.cfg.event = "click";
                }

                this.display.on(this.cfg.event, function(){
                    if (isDoubleTap) {
                        if (((new Date().getTime()) - touchtime) < 500) {
                           $this.show();
                        }
                        touchtime = new Date().getTime();
                    }
                    else {
                         $this.show();
                    }
                }).on("mouseover", function(){
                    $(this).toggleClass("ui-state-highlight");
                }).on("mouseout", function(){
                    $(this).toggleClass("ui-state-highlight");
                });
                if (this.display.attr("tabindex") >= 0) {
                    this.display.on("keydown", function(e){
                        if (PrimeFaces.utils.isActionKey(e)) {
                            $this.display.trigger($this.cfg.event);
                            e.preventDefault();
                        }
                    }).on("focus", function(){
                        $(this).toggleClass("ui-state-focus");
                    }).on("blur", function(){
                        $(this).toggleClass("ui-state-focus");
                    });
                }
            }
            else {
                this.display.css('cursor', 'default');
            }

            if(this.cfg.editor) {
                this.cfg.formId = this.jq.parents('form:first').attr('id');

                this.editor = $(this.jqId + '_editor');

                var saveButton = this.editor.children('.ui-inplace-save'),
                    cancelButton = this.editor.children('.ui-inplace-cancel');

                PrimeFaces.skinButton(saveButton).skinButton(cancelButton);

                saveButton.on("click", function(e) {
                    $this.save(e);
                });
                cancelButton.on("click", function(e) {
                    $this.cancel(e);
                });
            }

            /* to enter space in inplace input within multi-selection dataTable */
            this.content.find('input:text,textarea').on('keydown.inplace-text', function(e) {
                if(e.key === ' ') {
                    e.stopPropagation();
                }
            });
        }
    }

    /**
     * Switches to editing mode and displays the inplace editor.
     */
    show() {
        this.toggle(this.content, this.display);
    }

    /**
     * Leaves editing mode and hides the inplace editor.
     */
    hide() {
        this.toggle(this.display, this.content);
    }

    /**
     * Hides the label and shows the inline content or inplace editor; or vice versa.
     * @private
     * @param {JQuery} elToShow Element to show, either the label or the inplace editor.
     * @param {JQuery} elToHide Element to hide, either the label or the inplace editor.
     */
    toggle(elToShow, elToHide) {
        var $this = this;

        if(this.cfg.effect === 'fade') {
            elToHide.fadeOut(this.cfg.effectSpeed, function() {
                elToShow.fadeIn($this.cfg.effectSpeed);
                $this.postShow();
            });
        }
        else if(this.cfg.effect === 'slide') {
            elToHide.slideUp(this.cfg.effectSpeed, function() {
                elToShow.slideDown($this.cfg.effectSpeed);
                $this.postShow();
            });
        }
        else if(this.cfg.effect === 'none') {
            elToHide.hide();
            elToShow.show();
            $this.postShow();
        }
    }

    /**
     * Callback that is invoked when the inline content or inplace editor is shown or hidden. Puts focus on the
     * appropriate element and makes sure the inline content is rendered correctly.
     * @private
     */
    postShow() {
        this.content.find('input:text,textarea').filter(':visible:enabled:first').trigger('focus').trigger('select');

        PrimeFaces.invokeDeferredRenders(this.id);
    }

    /**
     * Fetches the display element, which is the container with the label or description shown when the inline content
     * is not displayed.
     * @return {JQuery} The display element or label when the editor is not shown.
     */
    getDisplay() {
        return this.display;
    }

    /**
     * Fetches the content element, which is the container element with the inline content or inplace editor.
     * @return {JQuery} The content element with the inplace editor.
     */
    getContent() {
        return this.content;
    }

    /**
     * When an inplace editor exists and it is currently active: saves the content of the editor and hides the inplace
     * editor.
     * @param {JQuery.TriggeredEvent} [e] The (click) event which triggered the saving. Currently unused.
     */
    save(e) {
        var options = {
            source: this.id,
            update: this.id,
            process: this.id,
            formId: this.cfg.formId
        };

        if(this.hasBehavior('save')) {
            this.callBehavior('save', options);
        }
        else {
            PrimeFaces.ajax.Request.handle(options);
        }
    }

    /**
     * When an inplace editor exists and it is currently active: discard changes that were made and hides the inplace
     * editor.
     * @param {JQuery.TriggeredEvent} [e] The (click) event which triggered the cancellation. Currently unused.
     */
    cancel(e) {
        var options = {
            source: this.id,
            update: this.id,
            process: this.id,
            formId: this.cfg.formId
        };

        options.params = [
            {name: this.id + '_cancel', value: true}
        ];

        if(this.hasBehavior('cancel')) {
            this.callBehavior('cancel', options);
        }
        else {
            PrimeFaces.ajax.Request.handle(options);
        }
    }

}
