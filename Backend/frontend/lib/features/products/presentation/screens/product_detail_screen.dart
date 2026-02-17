import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../products_provider.dart';
import '../../data/product_models.dart';
import '../../../shop/presentation/cart_provider.dart';

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
