import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../products_provider.dart';
import '../../data/product_models.dart';
import '../../../shop/presentation/cart_provider.dart';
import '../reviews_provider.dart';
import '../../../events/presentation/events_provider.dart';

class ProductDetailScreen extends StatefulWidget {
  final String productId;

  const ProductDetailScreen({super.key, required this.productId});

  @override
  State<ProductDetailScreen> createState() => _ProductDetailScreenState();
}

class _ProductDetailScreenState extends State<ProductDetailScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<ProductsProvider>().fetchProductDetails(widget.productId);
      context.read<ReviewsProvider>().fetchReviews(widget.productId);
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Detalle')),
      body: Consumer<ProductsProvider>(
        builder: (context, provider, child) {
          if (provider.isLoading) {
            return const Center(child: CircularProgressIndicator());
          }

          if (provider.error != null) {
            return Center(child: Text(provider.error!));
          }

          final product = provider.selectedProduct;
          if (product == null) {
            return const Center(child: Text('Producto no encontrado'));
          }

          // Use first variant for now
          final ProductVariant? mainVariant = product.variants.isNotEmpty ? product.variants.first : null;

          return SingleChildScrollView(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                Container(
                  height: 300,
                  color: Colors.grey[200],
                  child: mainVariant?.imageUrl != null
                      ? Image.network(mainVariant!.imageUrl!, fit: BoxFit.cover)
                      : const Icon(Icons.image, size: 100, color: Colors.grey),
                ),
                Padding(
                  padding: const EdgeInsets.all(16.0),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        product.nameTemplate,
                        style: Theme.of(context).textTheme.headlineSmall,
                      ),
                      const SizedBox(height: 4),
                      Row(
                        children: [
                          Row(
                            children: List.generate(5, (index) {
                              return Icon(
                                index < product.averageRating.floor()
                                    ? Icons.star
                                    : Icons.star_border,
                                color: Colors.amber,
                                size: 20,
                              );
                            }),
                          ),
                          const SizedBox(width: 8),
                          Text(
                            '${product.averageRating.toStringAsFixed(1)} (${product.reviewCount})',
                            style: Theme.of(context).textTheme.bodyMedium,
                          ),
                        ],
                      ),
                      const SizedBox(height: 8),
                      Text(
                        '\$${mainVariant?.rentalPrice.toStringAsFixed(2) ?? "0.00"}',
                        style: Theme.of(context).textTheme.titleLarge?.copyWith(
                              color: Theme.of(context).primaryColor,
                            ),
                      ),
                      const SizedBox(height: 8),
                      Chip(
                        label: Text('Stock Total: ${product.stockQuantity}'),
                        avatar: const Icon(Icons.inventory, size: 16),
                        backgroundColor: Colors.blue[50],
                      ),
                      const SizedBox(height: 16),
                      Text(
                        product.descriptionTemplate ?? 'Sin descripción',
                        style: Theme.of(context).textTheme.bodyLarge,
                      ),
                      const SizedBox(height: 24),
                      SizedBox(
                        width: double.infinity,
                        child: ElevatedButton(
                          onPressed: () async {
                            if (mainVariant == null) return;
                            
                            try {
                              await context.read<CartProvider>().addItem(
                                    product.id,
                                    mainVariant.id,
                                    1
                                  );
                              if (context.mounted) {
                                ScaffoldMessenger.of(context).showSnackBar(
                                  const SnackBar(content: Text('Añadido al carrito')),
                                );
                              }
                            } catch (e) {
                              if (context.mounted) {
                                ScaffoldMessenger.of(context).showSnackBar(
                                  SnackBar(content: Text('Error: $e')),
                                );
                              }
                            }
                          },

                          child: const Text('Añadir al Carrito'),
                        ),
                      ),
                      const SizedBox(height: 16),
                      SizedBox(
                        width: double.infinity,
                        child: OutlinedButton(
                          onPressed: () {
                            if (mainVariant == null) return;
                            _showAddToEventDialog(context, product.id);
                          },
                          child: const Text('Agregar a Evento'),
                        ),
                      ),
                      const SizedBox(height: 32),
                      const Divider(),
                      const SizedBox(height: 16),
                      Text(
                        'Reseñas',
                        style: Theme.of(context).textTheme.titleLarge,
                      ),
                      const SizedBox(height: 8),
                      Consumer<ReviewsProvider>(
                        builder: (context, reviewsProvider, child) {
                          if (reviewsProvider.isLoading && reviewsProvider.reviews.isEmpty) {
                            return const Center(child: Padding(
                              padding: EdgeInsets.all(16.0),
                              child: CircularProgressIndicator(),
                            ));
                          }
                          
                          if (reviewsProvider.reviews.isEmpty) {
                            return const Padding(
                              padding: EdgeInsets.symmetric(vertical: 16.0),
                              child: Text('Aún no hay reseñas para este producto.'),
                            );
                          }

                          return ListView.builder(
                            shrinkWrap: true,
                            physics: const NeverScrollableScrollPhysics(),
                            itemCount: reviewsProvider.reviews.length,
                            itemBuilder: (context, index) {
                              final review = reviewsProvider.reviews[index];
                              return Card(
                                margin: const EdgeInsets.symmetric(vertical: 8),
                                child: Padding(
                                  padding: const EdgeInsets.all(12.0),
                                  child: Column(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: [
                                      Row(
                                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                        children: [
                                          Text(
                                            review.user?.userName ?? 'Usuario',
                                            style: const TextStyle(fontWeight: FontWeight.bold),
                                          ),
                                          Text(
                                            review.created.toString().split(' ')[0],
                                            style: Theme.of(context).textTheme.bodySmall,
                                          ),
                                        ],
                                      ),
                                      const SizedBox(height: 4),
                                      Row(
                                        children: List.generate(5, (i) {
                                          return Icon(
                                            i < review.rating ? Icons.star : Icons.star_border,
                                            color: Colors.amber,
                                            size: 16,
                                          );
                                        }),
                                      ),
                                      const SizedBox(height: 8),
                                      Text(review.comment),
                                    ],
                                  ),
                                ),
                              );
                            },
                          );
                        },
                      ),
                      const SizedBox(height: 16),
                      ElevatedButton.icon(
                        onPressed: () => _showAddReviewDialog(context, product.id),
                        icon: const Icon(Icons.rate_review),
                        label: const Text('Escribir Reseña'),
                        style: ElevatedButton.styleFrom(
                          backgroundColor: Colors.grey[200],
                          foregroundColor: Colors.black,
                        ),
                      ),
                      const SizedBox(height: 32),
                    ],
                  ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }

  void _showAddReviewDialog(BuildContext context, String articleId) {
    int selectedRating = 5;
    final commentController = TextEditingController();

    showDialog(
      context: context,
      builder: (context) {
        return StatefulBuilder(
          builder: (context, setState) {
            return AlertDialog(
              title: const Text('Escribir Reseña'),
              content: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: List.generate(5, (index) {
                      return IconButton(
                        icon: Icon(
                          index < selectedRating ? Icons.star : Icons.star_border,
                          color: Colors.amber,
                        ),
                        onPressed: () {
                          setState(() {
                            selectedRating = index + 1;
                          });
                        },
                      );
                    }),
                  ),
                  TextField(
                    controller: commentController,
                    decoration: const InputDecoration(
                      hintText: 'Tu comentario...',
                      border: OutlineInputBorder(),
                    ),
                    maxLines: 3,
                  ),
                ],
              ),
              actions: [
                TextButton(
                  onPressed: () => Navigator.pop(context),
                  child: const Text('Cancelar'),
                ),
                ElevatedButton(
                  onPressed: () async {
                    if (commentController.text.isEmpty) return;
                    
                    final reviewsProvider = context.read<ReviewsProvider>();
                    await reviewsProvider.addReview(
                      articleId,
                      selectedRating,
                      commentController.text,
                    );
                    
                    if (context.mounted) {
                      Navigator.pop(context);
                      // Update product details to refresh average rating
                      context.read<ProductsProvider>().fetchProductDetails(articleId);
                    }
                  },
                  child: const Text('Enviar'),
                ),
              ],
            );
          },
        );
      },
    );
  }

  void _showAddToEventDialog(BuildContext context, String articleId) {
    showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: const Text('Agregar a Evento'),
          content: SizedBox(
            width: double.maxFinite,
            child: Consumer<EventsProvider>(
              builder: (context, eventsProvider, child) {
                if (eventsProvider.isLoading) {
                  return const Center(child: CircularProgressIndicator());
                }
                if (eventsProvider.events.isEmpty) {
                  return const Text('No tienes eventos creados.');
                }
                return ListView.builder(
                  shrinkWrap: true,
                  itemCount: eventsProvider.events.length,
                  itemBuilder: (context, index) {
                    final event = eventsProvider.events[index];
                    return ListTile(
                      title: Text(event.name),
                      subtitle: Text(event.date.toString().split(' ')[0]),
                      onTap: () {
                        // Add to event
                        eventsProvider.addItemToEvent(event.id, articleId, 1).then((success) {
                          Navigator.pop(context);
                          ScaffoldMessenger.of(context).showSnackBar(
                            SnackBar(content: Text(success ? 'Producto agregado a ${event.name}' : 'Error al agregar')),
                          );
                        });
                      },
                    );
                  },
                );
              },
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text('Cerrar'),
            ),
          ],
        );
      },
    );
  }
}
