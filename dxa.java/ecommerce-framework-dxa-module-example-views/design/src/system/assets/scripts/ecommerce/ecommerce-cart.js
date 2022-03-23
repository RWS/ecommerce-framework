$(document).ready(function() {

    $(".add-to-cart-button").click(function () {
        var productId = $(this).data("product-id");
        var localizationPrefix = $(this).data("localization-prefix");
        window.console.log("Adding to cart: " + productId);

        $.ajax({
            url: localizationPrefix + "ajax/cart/addProduct/" + productId,
            dataType: "text",
            success: function (data) {

                var cart = $(".cart-btn");

                var imgtodrag = $("#product-image");

                var imgclone = imgtodrag.clone()
                    .offset({
                        top: imgtodrag.offset().top,
                        left: imgtodrag.offset().left
                    })
                    .css({
                        'opacity': '0.5',
                        'position': 'absolute',
                        'height': '150px',
                        'width': '150px',
                        'z-index': '100'
                    })
                    .appendTo($('body'))
                    .animate({
                        'top': cart.offset().top + 10,
                        'left': cart.offset().left + 10,
                        'width': 75,
                        'height': 75
                    }, 1000, 'easeInOutExpo');

                setTimeout(function () {
                    cart.effect("shake", {
                        times: 2
                    }, 200);
                    $("#cart-amount").text(data);
                }, 1500);

                imgclone.animate({
                    'width': 0,
                    'height': 0
                }, function () {
                    $(this).detach()
                });

            }
        });

        return false;
    });

    $(".remove-from-cart").click(function() {
        var productId = $(this).data("product-id");
        var localizationPrefix = $(this).data("localization-prefix");
        window.console.log("Removing from cart: " + productId);

        $.ajax({
            url: localizationPrefix + "ajax/cart/removeProduct/" + productId,
            dataType: "text",
            success: function (data) {
                // Reload page to recalculate the cart
                //
                window.location.reload();
            }
        });

        return false;

    });
});
